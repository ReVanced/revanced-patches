package app.revanced.patches.youtube.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.shared.layout.branding.mipmapDirectories
import java.nio.file.Files

private const val APP_NAME = "YouTube ReVanced"

private val youtubeIconResourceFileNames_19_34 = mapOf(
    "adaptiveproduct_youtube_foreground_color_108" to "adaptiveproduct_youtube_2024_q4_foreground_color_108",
    "adaptiveproduct_youtube_background_color_108" to "adaptiveproduct_youtube_2024_q4_background_color_108",
)

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
        "adaptiveproduct_youtube_2024_q4_background_color_108.xml",
        "adaptiveproduct_youtube_2024_q4_foreground_color_108.xml",
        "adaptiveproduct_youtube_background_color_108.xml",
        "adaptiveproduct_youtube_foreground_color_108.xml",
    ),
    legacyIconResourceFileNames = arrayOf(
        "ic_launcher",
        "ic_launcher_round",
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
        val resourceDirectory = get("res")

        mipmapDirectories.forEach { directory ->
            val targetDirectory = resourceDirectory.resolve(directory)

            youtubeIconResourceFileNames_19_34.forEach { (old, new) ->
                val oldFile = targetDirectory.resolve("$old.png")
                val newFile = targetDirectory.resolve("$new.png")

                Files.write(newFile.toPath(), oldFile.readBytes())
            }
        }
    }
)
