#!/usr/bin/env bash
# Builds the release APK, installs it on an Android TV emulator and opens it.
#   scripts/install-emulator-tv.sh                         # boots AVD "Television_4K" (or reuses a running TV emulator)
#   TV_AVD=My_TV_AVD scripts/install-emulator-tv.sh
#   SET_AS_HOME=1 scripts/install-emulator-tv.sh           # also make it the default Home app
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE="com.techascent.tv_launcher"
COMPONENT="$PACKAGE/.MainActivity"
APK="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" && -f "$PROJECT_ROOT/local.properties" ]]; then
    SDK="$(sed -n 's/^sdk\.dir=//p' "$PROJECT_ROOT/local.properties" | tr -d '\r' | tail -1)"
fi
[[ -d "$SDK" ]] || { echo "Android SDK not found; set ANDROID_HOME" >&2; exit 1; }
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"
AVD="${TV_AVD:-Television_4K}"

is_tv_emulator() {
    "$ADB" -s "$1" shell pm has-feature android.software.leanback 2>/dev/null | grep -q true
}

SERIAL="${TV_SERIAL:-}"
if [[ -z "$SERIAL" ]]; then
    for s in $("$ADB" devices | awk '$1 ~ /^emulator-/ && $2 == "device" { print $1 }'); do
        if is_tv_emulator "$s"; then SERIAL="$s"; break; fi
    done
fi

if [[ -z "$SERIAL" ]]; then
    "$EMULATOR" -list-avds | grep -qx "$AVD" \
        || { echo "AVD '$AVD' not found. Available: $("$EMULATOR" -list-avds | tr '\n' ' ')" >&2; exit 1; }
    BEFORE="$("$ADB" devices | awk '$1 ~ /^emulator-/ { print $1 }')"
    echo "Booting AVD $AVD..."
    nohup "$EMULATOR" -avd "$AVD" -no-boot-anim >/dev/null 2>&1 &
    for _ in $(seq 1 60); do
        SERIAL="$("$ADB" devices | awk '$1 ~ /^emulator-/ { print $1 }' | grep -vxF "$BEFORE" | head -1 || true)"
        [[ -n "$SERIAL" ]] && break
        sleep 2
    done
    [[ -n "$SERIAL" ]] || { echo "Emulator did not appear in adb" >&2; exit 1; }
fi

echo "Waiting for $SERIAL to boot..."
"$ADB" -s "$SERIAL" wait-for-device
for _ in $(seq 1 180); do
    [[ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]] && break
    sleep 2
done
[[ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed | tr -d '\r')" == "1" ]] \
    || { echo "Emulator did not finish booting" >&2; exit 1; }
is_tv_emulator "$SERIAL" || { echo "$SERIAL is not an Android TV emulator" >&2; exit 1; }

API="$("$ADB" -s "$SERIAL" shell getprop ro.build.version.sdk | tr -d '\r')"
(( API >= 29 )) || { echo "Launcher requires API 29+ (emulator is API $API)" >&2; exit 1; }

cd "$PROJECT_ROOT"
echo "Building release APK..."
./gradlew :app:assembleRelease
echo "Installing on $SERIAL (API $API)..."
"$ADB" -s "$SERIAL" install -r "$APK"

if [[ "${SET_AS_HOME:-0}" == "1" ]]; then
    "$ADB" -s "$SERIAL" shell cmd role add-role-holder android.app.role.HOME "$PACKAGE"
    echo "Set as default Home app."
fi

"$ADB" -s "$SERIAL" shell am force-stop "$PACKAGE"
"$ADB" -s "$SERIAL" shell am start -W -n "$COMPONENT" >/dev/null
echo "Launcher opened on $SERIAL."