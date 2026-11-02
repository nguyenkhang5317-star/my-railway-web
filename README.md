# SMART OPTIMIZE — Android Smart Device Optimizer

A lightweight, honest Android device companion built with **Kotlin + Jetpack Compose (Material 3)**, MVVM, Coroutines/Flow and DataStore. It shows **real** RAM / storage / battery / device data, helps you review installed apps, and guides you to the official Android Settings pages for anything Android reserves for the user. It never fakes results.

> **Download the APK:** see the [Releases](https://github.com/nguyenkhang5317-star/my-railway-web/releases) page (`SmartOptimize-v1.0.0-debug.apk`), or the `smart-optimize-debug-apk` artifact of any successful "Build APK" workflow run.

---

## A. Project summary

- **App name:** Smart Optimize (`com.smartoptimize.app`), version **1.0.0** (versionCode 1)
- **Goal:** Monitor device health, manage installed apps and background-activity review, open the right system settings fast — with a minimal one-hand UI, no account, no internet, negligible resource use.
- **Platform:** Android 8.0+ (minSdk 26), target/compile SDK 34
- **Stack:** Kotlin 2.0.20 · AGP 8.5.2 · Gradle 8.7 · Compose BOM 2024.09.00 · Navigation Compose 2.7.7 · Lifecycle 2.8.4 · DataStore 1.1.1 · Coroutines 1.8.1 · JDK 17

## B. Feature status

| Feature | Status | Implementation |
|---|---|---|
| Dashboard — RAM (total/available/%/state/updated + Refresh) | ✅ Done | `ActivityManager.MemoryInfo` |
| Dashboard — Storage (total/used/free/% + donut chart) | ✅ Done | `StatFs` |
| Dashboard — Battery (level/charging/temp/voltage/health) | ✅ Done | `BatteryManager` + `ACTION_BATTERY_CHANGED`; missing values show "Not reported" |
| Device info (manufacturer/model/Android/API/CPU ABI) | ✅ Done | `android.os.Build` |
| Background App Manager (search, filter chips, grouping) | ✅ Done | `PackageManager` launcher visibility + `UsageStatsManager` |
| App detail (info, usage, size, protect toggle, App Info) | ✅ Done | `StorageStatsManager`, `ACTION_APPLICATION_DETAILS_SETTINGS` |
| Protected ("keep") apps list | ✅ Done | DataStore; default launcher + Smart Optimize always protected |
| Foreground App Focus | ✅ Done (honest scope) | Most-recent app from usage data + per-app "Manage" shortcuts + guidance. **Never claims to stop other apps** |
| Smart Optimize (OPTIMIZE NOW → recommendations) | ✅ Done | Re-measures real data, rule engine, user picks each action |
| Storage Manager (largest & rarely used apps, settings shortcut) | ✅ Done | `StorageStatsManager.queryStatsForPackage` (needs Usage Access) |
| Battery Manager (details + recent apps + settings shortcut) | ✅ Done | Public APIs only |
| Settings / Permission Guide / About (+ privacy summary) | ✅ Done | — |
| Unit tests (formatters, grouping, analysis rules) | ✅ Done — 16 tests passing in CI | JUnit 4 |
| Instrumentation smoke test | ✅ Written (not run in CI — needs emulator) | Compose UI Test |
| Debug APK (installable) | ✅ Built by GitHub Actions | Releases / workflow artifacts |
| Release APK (signed, store-ready) | ⚠️ Config compiles; signing keys not provided | See "Release signing" below |
| Force-stop / kill other apps | ❌ Not possible for normal APKs | Uses official App Info pages instead |
| Clear other apps' cache/data | ❌ Not possible for normal APKs | Guides user via App Info page |
| Exact per-app battery drain | ❌ System-only data | Links to Android battery settings |
| Battery wear (% health estimate) | ❌ Not reliably exposed | Never guessed from charge level |
| Disable system apps | ❌ Not implemented (unsafe + restricted) | System apps labelled, review carefully |

## C. UI preview

Screenshots require a physical device/emulator, which this build environment does not provide — none are fabricated. Screens implemented (Material 3, bottom nav: **Home · Apps · Battery · Settings**):

1. **Home** — device status, RAM / Storage (donut) / Battery cards, `SMART OPTIMIZE NOW`, Focus shortcut
2. **Apps (Background App Manager)** — search + filters (All / Recently used / Rarely used / Needs attention / System / Protected)
3. **App detail** — info, 30-day usage, storage size, protect switch, "Open App Info"
4. **Foreground App Focus** — current focus app, other recent apps with Manage buttons, honest limits card, guidance
5. **Smart Optimize** — checklist, OPTIMIZE NOW, severity-sorted recommendation cards with real actions
6. **Storage Manager** — donut + totals, largest apps, rarely used apps
7. **Battery Manager** — level, status, temperature, voltage, health, recent apps
8. **Settings** — Usage Access status, Focus switch, protected count
9. **Permission guide** & **About** (privacy + limitations)

Install the APK from Releases to preview on a real device.

## D. Source code structure

```
app/src/main/java/com/smartoptimize/app/
├── MainActivity.kt · SmartOptimizeApplication.kt · AppContainer.kt
├── navigation/            AppNavHost (Compose Navigation + bottom bar)
├── ui/
│   ├── home/ apps/ battery/ storage/ optimize/ focus/ settings/
│   ├── components/        cards, donut chart, app rows/icons, states
│   └── theme/             Material 3 color/typography/theme
├── domain/
│   ├── model/             pure-Kotlin models (null = "not reported")
│   ├── repository/        interfaces
│   └── usecase/           AnalyzeDeviceUseCase, AppGrouping (unit-tested)
├── data/
│   ├── repository/        implementations (IO dispatcher)
│   └── preferences/       DataStore (protected apps, focus switch)
├── platform/
│   ├── memory/ storage/ battery/ device/ pkg/ usage/
├── permissions/           SettingsNavigator (checked Settings intents)
└── utils/                 Formatters (unit-tested), ViewModel factory
app/src/test/              16 JVM unit tests
app/src/androidTest/       Compose launch smoke test
.github/workflows/         Build APK (tests + APK + Release on tags)
```

MVVM with `StateFlow` UI state; manual DI container (no framework overhead). Data refreshes on screen open / explicit Refresh only — **no background service, no polling, no WakeLock, no boot receiver, no INTERNET permission, no `QUERY_ALL_PACKAGES`** (launcher `<queries>` visibility only), no root, no hidden APIs, no Accessibility hacks.

## E. APK file

- **Debug APK (installable, debug-key signed):** [GitHub Releases](https://github.com/nguyenkhang5317-star/my-railway-web/releases) → `SmartOptimize-v1.0.0-debug.apk`, produced by the "Build APK" workflow. Every push also uploads it as the `smart-optimize-debug-apk` workflow artifact.
- Install: download on the phone → open the file → allow "install from this source" if prompted. Works on Android 8.0+.

## F. Build instructions

**Requirements:** JDK 17, Android SDK platform 34 + build-tools 34.0.0.

```bash
git clone https://github.com/nguyenkhang5317-star/my-railway-web.git
cd my-railway-web
./gradlew :app:testDebugUnitTest   # unit tests
./gradlew :app:assembleDebug       # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:connectedDebugAndroidTest   # optional, needs device/emulator
```

Or simply open the project in **Android Studio** and press Run. Or push/tag and let GitHub Actions do it: `git tag v1.0.1 && git push origin v1.0.1` publishes a Release with the APK attached.

### Release signing (for distribution)

```bash
keytool -genkeypair -v -keystore smartoptimize.keystore -alias smartoptimize \
  -keyalg RSA -keysize 2048 -validity 10000
```

Create `keystore.properties` (git-ignored) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`, add a `signingConfigs.release` block in `app/build.gradle.kts` reading it, then `./gradlew :app:assembleRelease` (R8 minify + resource shrinking are already enabled and verified to compile).

## G. Test report

Environment: GitHub Actions `ubuntu-latest`, JDK 17, Gradle 8.7, SDK 34 (no emulator available, so instrumentation tests were compiled but not executed, and on-device behaviors — real sensor values, OEM settings pages — still need a device pass).

| Test case | Expected | Actual | Status |
|---|---|---|---|
| Gradle build `:app:assembleDebug` | APK produced | APK produced & uploaded | ✅ Pass |
| Gradle build `:app:assembleRelease` (R8) | Compiles | Compiled (unsigned) | ✅ Pass |
| 7 × Formatters unit tests | Correct bytes/duration/relative/% | All pass | ✅ Pass |
| 4 × AppGrouping unit tests | Recent <24h; rarely-used needs usage data; system/protected excluded | All pass | ✅ Pass |
| 5 × AnalyzeDevice unit tests | Critical storage ≥90%; hot battery warning; severity ordering; no fake cleanup claims | All pass | ✅ Pass |
| Compose smoke test (launch + nav shell) | Renders on device/emulator | Written; not executed (no emulator in CI) | ⚠️ Not run |
| On-device: real RAM/storage/battery values | Match system Settings | Requires a physical device | ⚠️ Not run |
| On-device: Usage Access grant/deny flows | Graceful states both ways | Requires a physical device | ⚠️ Not run |
| On-device: Settings intents across OEMs | Opens or shows manual hint | Requires physical devices | ⚠️ Not run |

## H. Known limitations

1. **Cannot force-stop other apps or clear their cache** — Android forbids this for normal apps; Smart Optimize opens the official App Info page instead and says so in-app.
2. **App list = launchable apps only.** Without `QUERY_ALL_PACKAGES` (deliberately not requested, per Play policy), non-launcher background-only packages are invisible.
3. **"Recently used" ≠ "currently running".** Usage data records past foreground sessions; Android does not expose a reliable live-process view to normal apps. The UI states this explicitly.
4. **Usage-derived features need Usage Access** (last-used, durations, per-app sizes, Focus detection). Everything degrades to an honest "no data" state without it.
5. **Total foreground time is approximate** — daily usage buckets are summed (labelled with "~" in UI).
6. **No exact per-app battery drain / battery wear %** — not exposed by public APIs; never estimated.
7. **Some OEMs** customize Settings pages; every intent is resolve-checked with a manual-instruction fallback.
8. **Release build is unsigned** until you add your keystore (instructions above). The distributed debug APK is for testing/sideloading.

## I. Next development phase (only real-value items)

1. On-device QA pass (Pixel + Samsung + Xiaomi) for settings-intent paths and usage-data edge cases; run the instrumentation suite there.
2. Signed release pipeline (keystore via CI secrets) + Play Store internal track.
3. Optional: `StorageStatsManager` cache-vs-data breakdown per app in App Detail.
4. Optional: export a local text device report (share sheet) — still zero network.
5. Vietnamese localization (`values-vi`) — all plumbing is already standard resources/Compose.

---

**Privacy:** no account, no ads, no analytics, no INTERNET permission. Only your Protected-app choices and the Focus switch are stored, on-device, in DataStore.
