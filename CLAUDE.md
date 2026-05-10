# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK (requires signing config)
./gradlew clean            # Clean build artifacts
```

No unit test framework currently configured. Manual testing required.

## Architecture

Android app — VPN Gate client. Fetches server list CSV from `VPN_GATE_API`, parses, caches in SQLite.

**Layers:**
- `data/` — DbHelper (SQLite), ServerDatabase (schema), ServerContract (content provider contract)
- `model/` — Server.java (data class)
- `ui/activity/` — MainActivity (server list + fetch), ServerDetailsActivity, SettingsActivity
- `ui/adapter/` — ServerAdapter (RecyclerView)
- `ui/fragment/` — LicensesDialogFragment
- `ui/widget/` — EmptyRecyclerView
- `util/` — CsvParser (CSV parsing), OvpnUtils (OpenVPN config), PlayStoreUtils

**Entry point:** MainActivity → fetches VPN Gate CSV → CsvParser.parse() → display in RecyclerView.

**Server persistence:** SQLite via DbHelper singleton. ServerContract defines table schema.

**VPN Gate API:** `http://www.vpngate.net/api/iphone/` (configurable via `BuildConfig.VPN_GATE_API`).

## Key Files

- `app/build.gradle` — app config, compileSdkVersion 26, targetSdkVersion 26
- `build.gradle` — root, applies Android plugin 3.0.0-alpha7
- `app/proguard/proguard-okhttp3.pro` — OkHttp3 ProGuard rules (release only)
- `AndroidManifest.xml` — INTERNET + storage permissions, FileProvider for APK sharing

## Dependencies

- `com.squareup.okhttp3:okhttp:3.8.0` — HTTP client
- `pub.devrel:easypermissions:0.4.2` — runtime permissions
- `com.badoo.mobile:android-weak-handler:1.1` — WeakHandler for delayed operations
- Android Support Library (design, cardview-v7)

## ProGuard

Release builds minify enabled. Custom rules in `app/proguard/proguard-okhttp3.pro` applied alongside default ProGuard config.
