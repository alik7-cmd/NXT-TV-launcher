#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS="$PROJECT_ROOT/local.properties"
read_prop() {
    [[ -f "$PROPS" ]] || return 0
    sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" "$PROPS" | tr -d '\r' | tail -1
}
TV_IP="${TV_IP:-$(read_prop tv.ip)}"
TV_PORT_PROP="${TV_PORT:-$(read_prop tv.port)}"
if [[ -z "$TV_IP" ]]; then
    echo "TV IP not set. Add 'tv.ip=<address>' to $PROPS (or run with TV_IP=...)." >&2
    exit 1
fi
TV_SERIAL="$TV_IP:${TV_PORT_PROP:-5555}"
TV_SERIAL="192.168.0.6:5555"
COMPONENT="com.techascent.tv_launcher/.MainActivity"
APK="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"

if command -v adb >/dev/null 2>&1; then
    ADB="$(command -v adb)"
elif [[ -x "${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb" ]]; then
    ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
else
    echo "adb not found. Install Android SDK Platform-Tools and add adb to PATH." >&2
    exit 1
fi

TV_HOST="${TV_SERIAL%:*}"
TV_PORT="${TV_SERIAL##*:}"
RELAY_PORT=15555
RELAY_DIR=""
RELAY_PID=""

cleanup() {
    if [[ -n "$RELAY_PID" ]]; then
        "$ADB" disconnect "$TV_SERIAL" >/dev/null 2>&1 || true
        kill "$RELAY_PID" 2>/dev/null || true
        pkill -P "$RELAY_PID" 2>/dev/null || true
    fi
    [[ -n "$RELAY_DIR" ]] && rm -rf "$RELAY_DIR"
}
trap cleanup EXIT

# macOS Local Network privacy can block third-party binaries like adb ("No route to host")
# while Apple's own nc is allowed. Relaying through localhost avoids that restriction.
start_local_relay() {
    RELAY_DIR="$(mktemp -d)"
    (
        while true; do
            rm -f "$RELAY_DIR/fifo"
            mkfifo "$RELAY_DIR/fifo"
            nc -l 127.0.0.1 "$RELAY_PORT" < "$RELAY_DIR/fifo" | nc "$TV_HOST" "$TV_PORT" > "$RELAY_DIR/fifo"
        done
    ) &
    RELAY_PID=$!
    disown "$RELAY_PID" 2>/dev/null || true
    sleep 1
}

echo "Connecting to the home TV ($TV_SERIAL)..."
CONNECT_OUTPUT="$("$ADB" connect "$TV_SERIAL" 2>&1 || true)"
echo "$CONNECT_OUTPUT"
if [[ "$CONNECT_OUTPUT" == *"No route to host"* ]] && command -v nc >/dev/null 2>&1 \
    && nc -z -G 5 "$TV_HOST" "$TV_PORT" 2>/dev/null; then
    echo "adb is blocked from the local network; retrying through a localhost relay..."
    start_local_relay
    TV_SERIAL="127.0.0.1:$RELAY_PORT"
    "$ADB" connect "$TV_SERIAL" || true
    sleep 2
fi
STATE="$("$ADB" devices | awk -v serial="$TV_SERIAL" '$1 == serial { print $2 }')"
if [[ "$STATE" != "device" ]]; then
    case "$STATE" in
        unauthorized)
            echo "ADB reached the TV, but this computer is not authorized." >&2
            echo "Accept the debugging prompt on the TV, then rerun this script." >&2
            ;;
        offline)
            echo "TV is listed but offline. Reconnect ADB or restart TV debugging." >&2
            ;;
        *)
            echo "TV is not connected to ADB (state: ${STATE:-missing})." >&2
            echo "ADB executable: $ADB" >&2
            echo "Check the TV IP, network reachability, and TCP debugging on port 5555." >&2
            if command -v nc >/dev/null 2>&1; then
                NC_TIMEOUT=(-w 5)
                if [[ "$(uname -s)" == "Darwin" ]]; then
                    NC_TIMEOUT+=(-G 5)
                fi
                if nc -z "${NC_TIMEOUT[@]}" "$TV_HOST" "$TV_PORT"; then
                    echo "TCP port 5555 is reachable, but ADB did not establish a connection." >&2
                    echo "This does not confirm a working ADB service or authorization." >&2
                    echo "If ADB reports 'No route to host', check macOS Local Network permissions for the app starting the ADB server." >&2
                    echo "Also check VPN/firewall restrictions and restart TV debugging." >&2
                else
                    echo "TCP port 5555 is not reachable from this computer." >&2
                    echo "Check that the TV is awake, its IP is unchanged, and network debugging is enabled." >&2
                fi
            fi
            ;;
    esac
    exit 1
fi

SDK="$("$ADB" -s "$TV_SERIAL" shell getprop ro.build.version.sdk | tr -d '\r\n')"
if [[ ! "$SDK" =~ ^[0-9]+$ ]]; then
    echo "Could not read the TV's Android API level: $SDK" >&2
    exit 1
fi
if (( SDK < 29 )); then
    echo "This launcher requires Android 10 / API 29 or newer (TV reports API $SDK)." >&2
    exit 1
fi

cd "$PROJECT_ROOT"
echo "Building the optimized release APK..."
./gradlew :app:assembleRelease
echo "Installing on the home TV (API $SDK)..."
"$ADB" -s "$TV_SERIAL" install -r "$APK"
"$ADB" -s "$TV_SERIAL" shell am force-stop com.techascent.tv_launcher
# The stock launcher can briefly take focus after an update, so retry the launch.
for attempt in 1 2 3; do
    "$ADB" -s "$TV_SERIAL" shell am start -W -n "$COMPONENT" >/dev/null
    sleep 3
    if "$ADB" -s "$TV_SERIAL" shell dumpsys activity activities \
        | grep -q "mResumedActivity.*$COMPONENT"; then
        echo "Launcher installed and opened. The default Home launcher was not changed."
        exit 0
    fi
done
echo "Installed, but the launcher did not come to the foreground. Open it from the TV's apps list." >&2
exit 1
