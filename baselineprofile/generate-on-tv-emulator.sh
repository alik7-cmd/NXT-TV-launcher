#!/usr/bin/env bash
# Generates the launcher Baseline Profile on an Android TV emulator.
#
# Gradle Managed Devices do not support TV images, so this boots an existing TV AVD headless and
# read-only (the AVD itself is left untouched), pins Gradle to it with ANDROID_SERIAL so no phone or
# real TV is used, runs :app:generateBaselineProfile and shuts down only the emulator it started.
#
#   baselineprofile/generate-on-tv-emulator.sh                 # boots AVD "Television_4K"
#   TV_AVD=My_TV_AVD baselineprofile/generate-on-tv-emulator.sh
#   TV_SERIAL=emulator-5554 baselineprofile/generate-on-tv-emulator.sh   # reuse a running TV emulator
#
# Output: app/src/release/generated/baselineProfiles/{baseline-prof.txt,startup-prof.txt}
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" && -f "$ROOT/local.properties" ]]; then
  SDK="$(sed -n 's/^sdk\.dir=//p' "$ROOT/local.properties" | tail -1)"
fi
[[ -d "$SDK" ]] || { echo "Android SDK not found; set ANDROID_HOME" >&2; exit 1; }
ADB="$SDK/platform-tools/adb"
EMULATOR="$SDK/emulator/emulator"

AVD="${TV_AVD:-Television_4K}"
PORT="${TV_EMULATOR_PORT:-5584}"
SERIAL="${TV_SERIAL:-}"
STARTED_PID=""

cleanup() {
  if [[ -n "$STARTED_PID" ]]; then
    echo "Stopping emulator $SERIAL"
    "$ADB" -s "$SERIAL" emu kill >/dev/null 2>&1 || true
    sleep 5
    kill "$STARTED_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

if [[ -z "$SERIAL" ]]; then
  SERIAL="emulator-$PORT"
  if "$ADB" devices | grep -q "^$SERIAL"; then
    echo "$SERIAL is already in use; set TV_SERIAL to reuse it or TV_EMULATOR_PORT to pick another port" >&2
    exit 1
  fi
  echo "Booting AVD $AVD as $SERIAL (headless, read-only)"
  "$EMULATOR" -avd "$AVD" -port "$PORT" -read-only -no-window -no-snapshot -no-audio -no-boot-anim \
    -gpu "${TV_EMULATOR_GPU:-auto}" >"$ROOT/build/tv-emulator.log" 2>&1 &
  STARTED_PID=$!
fi

"$ADB" -s "$SERIAL" wait-for-device
for _ in $(seq 1 180); do
  [[ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]] && break
  sleep 2
done
[[ "$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed | tr -d '\r')" == "1" ]] || { echo "Emulator did not boot" >&2; exit 1; }
"$ADB" -s "$SERIAL" shell pm has-feature android.software.leanback | grep -q true \
  || { echo "$SERIAL is not an Android TV device" >&2; exit 1; }
QEMU="$("$ADB" -s "$SERIAL" shell 'getprop ro.boot.qemu; getprop ro.kernel.qemu' | tr -d '\r')"
grep -qx 1 <<<"$QEMU" \
  || { echo "$SERIAL is not an emulator; refusing to use a physical device" >&2; exit 1; }

cd "$ROOT"
ANDROID_SERIAL="$SERIAL" ./gradlew :app:generateBaselineProfile "$@"
echo "Profiles:"
ls -l app/src/release/generated/baselineProfiles/
