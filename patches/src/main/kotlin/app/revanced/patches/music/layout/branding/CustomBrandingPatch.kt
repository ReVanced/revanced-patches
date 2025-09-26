package app.revanced.patches.music.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.shared.layout.branding.musicIconResourceFileNames

private const val APP_NAME = "YT Music ReVanced"

@Suppress("unused")
val customBrandingPatch = baseCustomBrandingPatch(
    defaultAppName = APP_NAME,
    appNameValues = mapOf(
        "YT Music ReVanced" to APP_NAME,
        "Music ReVanced" to "Music ReVanced",
        "Music" to "Music",
        "YT Music" to "YT Music",
    ),
    iconResourceFileNames = musicIconResourceFileNames,
    resourceFolder = "custom-branding/music",

    block = {
        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    }
)
