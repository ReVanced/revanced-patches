package app.revanced.patches.music.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch

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
    resourceFolder = "custom-branding/music",
    iconResourceFileNames = arrayOf(
        "adaptiveproduct_youtube_music_2024_q4_background_color_108",
        "adaptiveproduct_youtube_music_2024_q4_foreground_color_108",
        "ic_launcher_release",
    ),
    launchScreenAnimationFileName = "app_launch.json",

    block = {
        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    }
)
