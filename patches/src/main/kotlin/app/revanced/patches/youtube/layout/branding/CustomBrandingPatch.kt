package app.revanced.patches.youtube.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import java.nio.file.Files

private const val APP_NAME = "YouTube ReVanced"

private const val ADAPTIVE_BACKGROUND_RESOURCE_FILE_NAME = "adaptiveproduct_youtube_background_color_108.xml"
private const val ADAPTIVE_FOREGROUND_RESOURCE_FILE_NAME = "adaptiveproduct_youtube_foreground_color_108.xml"

@Suppress("unused")
val customBrandingPatch = baseCustomBrandingPatch(
    defaultAppName = APP_NAME,
    appNameValues = mapOf(
        "YouTube ReVanced" to APP_NAME,
        "YT ReVanced" to "YT ReVanced",
        "YT" to "YT",
        "YouTube" to "YouTube",
    ),
    resourceFolder = "custom-branding/youtube",
    iconResourceFileNames = arrayOf(
        "adaptiveproduct_youtube_background_color_108",
        "adaptiveproduct_youtube_foreground_color_108",
        "ic_launcher",
        "ic_launcher_round",
    ),
    monochromeIconFileNames = arrayOf(
        "adaptive_monochrome_ic_youtube_launcher.xml",
        "ringo2_adaptive_monochrome_ic_youtube_launcher.xml"
    ),
    adaptiveIconFileNames = arrayOf(
        ADAPTIVE_BACKGROUND_RESOURCE_FILE_NAME,
        ADAPTIVE_FOREGROUND_RESOURCE_FILE_NAME,
    ),
    legacyIconResourceFileNames = arrayOf(
        "ic_launcher",
        // "ic_launcher_round" also exists in 19.34, but was removed in later targets.
    ),

    block = {
        compatibleWith(
            "com.google.android.youtube"(
                "19.34.42",
                "20.07.39",
                "20.13.41",
                "20.14.43",
            )
        )
    },

    executeBlock = {
        val resourceDirectory = get("res/mipmap-anydpi")

        // Copy adaptive icon to secondary adaptive file.
        arrayOf(
            ADAPTIVE_BACKGROUND_RESOURCE_FILE_NAME to "adaptiveproduct_youtube_2024_q4_background_color_108.xml",
            ADAPTIVE_FOREGROUND_RESOURCE_FILE_NAME to "adaptiveproduct_youtube_2024_q4_foreground_color_108.xml",
        ).forEach { (old, new) ->
            val oldFile = resourceDirectory.resolve(old)
            val newFile = resourceDirectory.resolve(new)

            Files.write(newFile.toPath(), oldFile.readBytes())
        }
    }
)
