package app.revanced.patches.youtube.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_MAIN_ACTIVITY_NAME
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

private const val APP_NAME = "YouTube ReVanced"

@Suppress("unused")
val customBrandingPatch = baseCustomBrandingPatch(
    defaultAppName = APP_NAME,
    appNameValues = mapOf(
        "YouTube ReVanced" to APP_NAME,
        "YT ReVanced" to "YT ReVanced",
        "YT" to "YT",
        "YouTube" to "YouTube",
    ),
    originalLauncherIconName = "ic_launcher",
    manifestAppLauncherValue = "@string/application_name",
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    mainActivityName = YOUTUBE_MAIN_ACTIVITY_NAME,
    activityAliasNameWithIntentToRemove = "com.google.android.youtube.app.honeycomb.Shell\$HomeActivity",
    preferenceScreen = PreferenceScreen.GENERAL_LAYOUT,

    block = {
        dependsOn(sharedExtensionPatch)

        compatibleWith(
            "com.google.android.youtube"(
                "19.34.42",
                "20.07.39",
                "20.13.41",
                "20.14.43",
            )
        )
    }
)
