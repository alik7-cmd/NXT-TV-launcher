# TV Launcher

A minimal, ad-free home screen for Android TV / Google TV, built with Jetpack Compose.

- Collapsing header: greeting, large clock (follows the TV's 12/24-hour setting) and date, plus Inputs and Settings buttons whose labels expand on focus. While scrolling it shrinks into a compact translucent bar; Back returns to the top.
- **Weather hero:** a soft weather scene (sun, moon, clouds, rain, snow, fog or thunder, by day or night) fades in behind the top-right corner with the temperature, condition, city, high/low and "Rain at …" when rain is likely in the next 12 hours. It fades out as the header collapses, and a small weather pill appears in the bar. Location is approximate, from the network's public IP (GeoJS, falling back to ipwho.is); the forecast comes from Open-Meteo. No API keys and no location permission are needed. It refreshes every 30 minutes while the launcher is visible and stays hidden when offline. The art animates gently (drifting cloud, pulsing glow, twinkling stars, falling rain and snow). Both can be switched off in `LauncherConfig.kt`: `useWeatherHero = false` removes weather from the screen, and `animateWeatherArt = false` keeps the art still.
- **Favourites** dock (one horizontal row) above an **All apps** grid that wraps to the screen width
- TV Material cards provide focus zoom, border, glow and long-press handling. The favourites row restores its last focused tile when revisited.
- Only TV-launchable apps (`LEANBACK_LAUNCHER`) are listed; phone-only apps are excluded, including from favourites.
- The background fills the entire screen without rounded window corners.
- The trailing **Add app to favourite** tile opens a full-height side panel of TV apps not already in favourites; its long label scrolls automatically. The panel shows a message when all apps have been added.
- Long-press a favourite for an anchored **Open** / **Remove from favourites** popup. All apps has an **Open** / **Uninstall** popup (uninstall is available for user-installed apps only); adding favourites is handled by the side panel.
- English and Norwegian (Bokmål)
- Supports Android 10 (API 29) and newer

## Requirements

| What | Version |
|------|---------|
| Android Studio | Recent stable, or a JDK 17+ for command-line builds |
| TV | Android TV / Google TV running **Android 10 or newer** (Settings → Device Preferences / System → About → Android TV OS version) |
| Computer | `adb` (bundled with the Android SDK at `~/Library/Android/sdk/platform-tools/adb` on macOS) |

The computer and the TV must be on the same network for wireless debugging.

## Home TV: 192.168.0.6 (Android 10)

This is the specific home device, already connected and authorized using network ADB at
**192.168.0.6:5555**. With the TV awake and your computer on the same home network, run:

```bash
bash scripts/install-home-tv.sh
```

The script connects to this TV, checks authorization and OS compatibility, builds the latest
optimized release APK (R8, signed with your local debug key so it updates the existing install), installs it while preserving app data, and restarts the launcher. It always targets
this TV explicitly, even if a phone is also connected. It **does not change the default Home
launcher** or disable any system apps.

If authorization is requested again, accept **Allow USB debugging?** on the TV, then rerun.
If macOS blocks `adb` from the local network (`No route to host` even though the TV is reachable),
the script automatically relays the connection through Apple's built-in `nc` on
`127.0.0.1:15555`, then stops the relay when it finishes.
If the router assigns a different IP, update `TV_SERIAL` in the script; a DHCP reservation for
`192.168.0.6` can keep the address stable. This script is specific to this device, not a general
Android 10 installer.

## 1. Build

```bash
./gradlew :app:assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest    # optional: ViewModel unit tests
```

Or in Android Studio, open the project and choose **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

## 2. Prepare the TV (one-time)

1. **Enable Developer options:** Settings → System → About → scroll to **Android TV OS build** and press OK on it
   7 times, until "You are now a developer" appears.
2. **Enable debugging:** Settings → System → Developer options:
   - Turn on **USB debugging**.
   - On Android 10, enable **Network debugging / ADB debugging** if your TV offers it.
   - On newer TVs, enable **Wireless debugging** if available; not all manufacturers expose it.
3. **Find the TV's IP address:** Settings → Network & Internet → (your network), or the Wireless debugging screen.

## 3. Connect from your computer

**Google TV / Android 11+ (wireless debugging with pairing):**

```bash
# On the TV: Wireless debugging → "Pair device with pairing code"
adb pair <TV_IP>:<PAIRING_PORT>     # enter the 6-digit code shown on the TV
adb connect <TV_IP>:<PORT>          # the port shown on the main Wireless debugging screen (not the pairing port)
adb devices                         # the TV should be listed as "device"
```

**TVs exposing network ADB (no pairing code):**

```bash
adb connect <TV_IP>:5555            # accept the "Allow USB debugging?" prompt on the TV
adb devices                        # <TV_IP>:5555 should appear as "device"
adb -s <TV_IP>:5555 shell getprop ro.build.version.sdk
# An Android 10 device reports 29
```

If connection is refused, verify that network/ADB debugging is enabled and both devices are
on the same trusted local network (guest Wi-Fi can block device-to-device connections).
USB debugging alone does not enable network ADB on every TV. If the TV supports a USB
**data/debugging** connection, connect that first, authorize your computer, then run:

```bash
adb devices
adb -s <USB_SERIAL> tcpip 5555
adb connect <TV_IP>:5555
```

Use only the manufacturer's supported USB debugging port/cable; ordinary USB host ports
on TVs are not necessarily debugging ports. If neither network nor USB debugging is
supported, copy the APK to a USB drive, open it with a TV file manager, and grant that
file manager **Install unknown apps** permission. You can launch the app this way, but
changing the default Home app may still require ADB or a manufacturer-supported setting.

## 4. Install and run

For other supported TVs, connect using the method exposed by the device, then use its ADB
serial (IP and port for a network connection):

```bash
adb -s <TV_IP>:<PORT> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <TV_IP>:<PORT> shell am start -n com.techascent.tv_launcher/.MainActivity
```

You can also select the TV in Android Studio's device dropdown and press **Run**.

The app also appears in the TV's own app list as **TV Launcher**, so you can test it before making it the
home screen.

`INSTALL_FAILED_OLDER_SDK` means the APK requires a newer OS. Use a newly built APK from this
project (minimum API 29), not an older APK that required API 31 or 36.
If installation reports an incompatible signature, use the original signing key or uninstall
the previous app first (which deletes its favourites/settings). Restore the stock Home app
before uninstalling if this launcher is already your default.

## 5. Make it the home screen

Pressing Home on the remote opens the default launcher. Android TV usually has no settings screen for
choosing another one, so set it with adb. Test opening apps, D-pad scrolling and Settings
before changing Home. Commands below assume only the TV is connected; if the phone is also
connected, add `-s <TV_IP>:5555` after **every** `adb` command.

First record the existing Home component so you can restore it:

```bash
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Save the returned `package/activity` as `<STOCK_HOME_COMPONENT>`.

### Option A — set it as the home activity (try first, reversible)

```bash
adb shell cmd package set-home-activity --user 0 com.techascent.tv_launcher/.MainActivity
adb shell input keyevent KEYCODE_HOME
```

Press Home on the remote and, if offered, choose **TV Launcher → Always**.
Some manufacturers prevent changing Home. If the stock launcher still opens, review Option B.

### Option B — disable the stock launcher

Some TVs override the chosen Home app. Disabling the stock launcher is an optional, higher-risk
workaround, not a required installation step. Vendor firmware may reject it. Keep ADB connected
and record the exact stock package and restore command first. Disable only the Home package
identified above; do not disable unrelated setup or system packages.

```bash
# Use the package from your recorded Home component, not a guessed package name:
adb shell pm disable-user --user 0 <STOCK_LAUNCHER_PACKAGE>

# Common examples (identify yours first):
# Classic Android TV, including many Android 10 TVs: com.google.android.tvlauncher
# Google TV: com.google.android.apps.tv.launcherx
```

Press Home, and pick **TV Launcher → Always** if the TV asks.

> ⚠️ Keep this launcher installed while the stock launcher is disabled. If you uninstall it, the TV may boot
> to a blank screen until you re-enable the stock launcher.

### Restore the original launcher

```bash
adb shell pm enable --user 0 <STOCK_LAUNCHER_PACKAGE>
adb shell cmd package set-home-activity --user 0 <STOCK_HOME_COMPONENT>
adb shell input keyevent KEYCODE_HOME
```

Some vendor features (the Google Assistant home panel, recommendations, the "For you" tab) depend on the
stock launcher and won't be available while it's disabled.

After setup, disable debugging when you no longer need it. Never expose ADB port 5555 to
the internet or enable it on an untrusted network.

## 6. What to test with the remote

| Action | Expected |
|--------|----------|
| Open the launcher | The first app (first favourite, otherwise the first app) is highlighted with a zoom, glow and bright border |
| D-pad ← → ↑ ↓ | The highlight moves between tiles. Up from the top row goes to the Inputs / Settings buttons, which show their label when focused |
| OK | Opens the app or the settings screen |
| Long-press OK on an app | Anchored popup: Open plus Remove from favourites (favourites row) or Uninstall (All apps; disabled for system apps) |
| Add a favourite | The app appears in the Favourites row at the top |
| Install or uninstall an app | The grid updates automatically |
| Scroll down into All apps | The header collapses into a compact bar with a small clock; moving up to Inputs/Settings or the favourites expands it again |
| Back while scrolled down | Returns to the top and highlights the first favourite |
| Weather | After a few seconds the top-right shows the weather art and local temperature; it fades into a small pill when scrolled down. With no internet, nothing is shown |
| Inputs button | Opens the system Inputs panel (HDMI and other sources), or main Settings on devices without one (e.g. streaming sticks) |

## Validation

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintRelease
```

For release frame timing, install an optimized release build, let startup settle, then reset
the frame statistics and browse with the D-pad:

```bash
adb -s <TV_SERIAL> shell dumpsys gfxinfo com.techascent.tv_launcher reset
# Move between rows and tiles with the remote.
adb -s <TV_SERIAL> shell dumpsys gfxinfo com.techascent.tv_launcher
```

On the physical TV, aim for roughly 10 ms median and 14 ms at the 90th percentile or better.
Emulator timings are not comparable to the TV's baseline. Compare raw screenshots before and
after Left/Right on the first and middle All apps rows: the page should shift by 0 px.

## Viewing logs

```bash
adb logcat -b crash                          # crashes
adb logcat | grep -i tv_launcher             # app logs
```

## Performance

Installed-app refreshes reuse decoded icons, banners and labels for unchanged packages.
Package broadcasts invalidate only the affected package; removed activities are evicted.
A resource-configuration change clears the cache on the next refresh, and starting a new
collector starts with a fresh cache. Bitmaps are not manually recycled while the UI may use them.

The weather scene uses one offscreen graphics layer for both its edge masks and its
scroll-driven fade/translation. Drawing objects are cached, but compositing still costs GPU
work. The weather scene, collapsing header and non-lazy app grid retain their existing behavior.
App tiles use TV Material 1.0.1 cards instead of custom focus, scale and shadow animations;
header actions use TV Material buttons with labels that still expand on focus. The existing
pivot scroll spec, snapping and 24 dp dead band remain in place to prevent sideways focus
moves from shaking the page. These changes do not establish a measured FPS or memory
improvement on a physical TV.

## Project structure

```
app/src/main/java/com/techascent/tv_launcher/
├── data/          # Repositories (PackageManager, SharedPreferences, weather HTTP) emitting DTOs as Flows
├── domain/        # Models, repository interfaces, use cases
├── presentation/  # ViewModel, UiState, effects, navigator and Compose UI
├── di/            # AppContainer (manual dependency injection)
└── ui/theme/      # LauncherColors, LauncherSpacing, theme
```
