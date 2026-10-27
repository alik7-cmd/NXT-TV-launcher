# TV Launcher

A simple, ad-free launcher for **Android TV and Google TV (Android 10+)**, built with Kotlin and Jetpack Compose.

Browse installed apps, keep pinned favorites in the collapsing header, and customize the home screen with circular icons or rectangular tiles, optional app names, and three wallpapers. Fully navigable with a TV remote.

Pin favorites in a horizontally scrolling row (in pin order), hide apps, or sort all apps alphabetically or by recent use. Pinned apps also remain in All apps. Hidden apps disappear from every home row; restore them through **Customize → Manage hidden apps**.

## Screenshots

Captured from the current app running on an Android TV emulator, with pinned apps enabled.

![Home screen with pinned apps](docs/screenshots/home.png)

| Customize | Wallpapers |
|-----------|------------|
| ![Icon style and app names](docs/screenshots/customize.png) | ![Wallpaper selector](docs/screenshots/wallpapers.png) |

**App actions:** open, pin/unpin, hide, or uninstall.

![App actions with Pin and Hide](docs/screenshots/app-options.png)

## Install

Build the APK using Android Studio or the command line (Android SDK and JDK 17+ required):

```bash
./gradlew :app:assembleDebug
```

The APK is at `app/build/outputs/apk/debug/app-debug.apk`. Alternatively, copy it to your TV and open it with a file manager allowed to **Install unknown apps**.

### Connect an Android TV

Enable Developer options by pressing **Build / Android TV OS build** seven times under **Settings → About**. Enable **ADB / Network debugging** if your device provides it, then connect from a computer on the same trusted network:

```bash
adb connect <TV_IP>:5555
```

Accept the debugging prompt on the TV. USB debugging alone does not enable network ADB on every TV; use the manufacturer's supported debugging method.

### Connect a Google TV

Enable Developer options under **Settings → System → About → Android TV OS build**, then enable **Wireless debugging** if available. Choose **Pair device with pairing code**:

```bash
adb pair <TV_IP>:<PAIRING_PORT>
adb connect <TV_IP>:<DEBUG_PORT>
```

Enter the pairing code when prompted. The pairing and debugging ports are different; use the values shown on the TV. If your Google TV only offers network ADB, use the Android TV method above.

### Install and open (both platforms)

```bash
adb devices
adb -s <TV_SERIAL> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <TV_SERIAL> shell am start -n com.techascent.tv_launcher/.MainActivity
```

Replace `<TV_SERIAL>` with the device identifier from `adb devices`, such as its IP and port. You can also open **TV Launcher** from the TV's app list.

**Optional default home:** first record your current home component, then try setting TV Launcher:

```bash
adb -s <TV_SERIAL> shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s <TV_SERIAL> shell cmd package set-home-activity --user 0 com.techascent.tv_launcher/.MainActivity
```

Save the first command's `package/activity` output. To restore it, run `set-home-activity --user 0 <ORIGINAL_HOME_COMPONENT>`. Home replacement depends on the device; some manufacturers block it. **Do not disable the stock launcher.** Turn off debugging when finished.

## Remote controls

| Control | Action |
|---------|--------|
| D-pad | Move between apps and header actions |
| OK | Open an app or select an option |
| Hold OK on an app | Open / Pin or Unpin / Hide / Uninstall (system apps cannot be uninstalled) |
| Customize icon | Change icon shape, app-name visibility, wallpaper, sorting, or restore hidden apps |
| Back | Dismiss a dialog or return to the top when scrolled |

Wallpaper choices are applied with **Apply**; customization is saved between launches.

Recently used sorting uses local launcher launch history only (no usage-access permission); never-launched apps follow alphabetically. Pins, hidden apps, sorting, and launch history are saved locally. Hiding keeps an app's pin position and history, so restoring brings them back. Uninstalled apps do not appear; their saved preferences are retained for reinstallation.
