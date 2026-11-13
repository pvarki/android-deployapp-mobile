# Implementation Plan - Edge-to-Edge Support for SettingsActivity

Add edge-to-edge support to `SettingsActivity` to ensure it looks modern and utilizes the full screen area, handling system bar insets correctly.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/development/timo/pvarki/android-deployapp-mobile/deployapp/gradle/libs.versions.toml)
- Add `androidx-activity-ktx` dependency.

#### [MODIFY] [build.gradle.kts](file:///D:/development/timo/pvarki/android-deployapp-mobile/deployapp/app/build.gradle.kts)
- Include `androidx-activity-ktx` in the dependencies.

### UI Components

#### [MODIFY] [SettingsActivity.kt](file:///D:/development/timo/pvarki/android-deployapp-mobile/deployapp/app/src/main/java/com/pvarki/deployapp/ui/SettingsActivity.kt)
- Call `enableEdgeToEdge()` in `onCreate`.
- Apply window insets to the root view to handle status and navigation bars.

#### [MODIFY] [activity_settings.xml](file:///D:/development/timo/pvarki/android-deployapp-mobile/deployapp/app/src/main/res/layout/activity_settings.xml)
- Add an ID to the root layout if missing (it's currently missing a root ID) to easily reference it for insets.

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Open `SettingsActivity`.
- Verify that the content is not obscured by the status bar or navigation bar.
- Verify that the background extends behind the system bars.
