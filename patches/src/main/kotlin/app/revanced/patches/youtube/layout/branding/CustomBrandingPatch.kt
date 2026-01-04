package app.revanced.patches.youtube.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_MAIN_ACTIVITY_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

@Suppress("unused")
val customBrandingPatch = baseCustomBrandingPatch(
    addResourcePatchName = "youtube",
    originalLauncherIconName = "ic_launcher",
    originalAppName = "@string/application_name",
    originalAppPackageName = YOUTUBE_PACKAGE_NAME,
    isYouTubeMusic = false,
    numberOfPresetAppNames = 5,
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    mainActivityName = YOUTUBE_MAIN_ACTIVITY_NAME,
    activityAliasNameWithIntents = "com.google.android.youtube.app.honeycomb.Shell\$HomeActivity",
    preferenceScreen = PreferenceScreen.GENERAL_LAYOUT,

    block = {
        dependsOn(sharedExtensionPatch)

        compatibleWith(
            "com.google.android.youtube"(
                "19.43.41",
                "20.14.43",
                "20.21.37",
                "20.31.40",
            )
        )
    }
)
