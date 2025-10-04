package app.revanced.patches.youtube.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.shared.layout.branding.mipmapDirectories
import java.nio.file.Files

private const val APP_NAME = "YouTube ReVanced"

private const val ADAPTIVE_BACKGROUND_RESOURCE_NAME = "adaptiveproduct_youtube_background_color_108"
private const val ADAPTIVE_FOREGROUND_RESOURCE_NAME = "adaptiveproduct_youtube_foreground_color_108"

@Suppress("unused")
val customBrandingPatch = baseCustomBrandingPatch(
    defaultAppName = APP_NAME,
    appNameValues = mapOf(
        "YouTube ReVanced" to APP_NAME,
        "YT ReVanced" to "YT ReVanced",
        "YT" to "YT",
        "YouTube" to "YouTube",
    ),
    patchResourceFolder = "custom-branding/youtube",
    adaptiveAnyDpiFileNames = arrayOf(
        "$ADAPTIVE_BACKGROUND_RESOURCE_NAME.xml",
        "$ADAPTIVE_FOREGROUND_RESOURCE_NAME.xml",
    ),
    adaptiveMipmapFileNames = arrayOf(
        "$ADAPTIVE_BACKGROUND_RESOURCE_NAME.png",
        "$ADAPTIVE_FOREGROUND_RESOURCE_NAME.png",
    ),
    legacyMipmapFileNames = arrayOf(
        "ic_launcher.png",
        // "ic_launcher_round" exists in 19.34, but was removed in later targets.
    ),
    monochromeFileNames = arrayOf(
        "adaptive_monochrome_ic_youtube_launcher.xml",
        "ringo2_adaptive_monochrome_ic_youtube_launcher.xml"
    ),
    manifestAppLauncherValue = "@string/application_name",

    block = {
        compatibleWith(
            "com.google.android.youtube"(
                "19.34.42",
                "20.07.39",
                "20.13.41",
                "20.14.43",
            )
        )
    }

) {
    val resourceDirectory = get("res")

    // Copy adaptive icon to secondary adaptive file.
    arrayOf(
        ADAPTIVE_BACKGROUND_RESOURCE_NAME to "adaptiveproduct_youtube_2024_q4_background_color_108",
        ADAPTIVE_FOREGROUND_RESOURCE_NAME to "adaptiveproduct_youtube_2024_q4_foreground_color_108",
    ).forEach { (old, new) ->
        var resourceType = "mipmap-anydpi"
        val oldFile = resourceDirectory.resolve("$resourceType/$old.xml")
        if (oldFile.exists()) {
            val newFile = resourceDirectory.resolve("$resourceType/$new.xml")
            Files.write(newFile.toPath(), oldFile.readBytes())
        }
    }

    // Copy mipmaps to secondary files.
    mipmapDirectories.forEach { directory ->
        val targetDirectory = resourceDirectory.resolve(directory)

        arrayOf(
            ADAPTIVE_BACKGROUND_RESOURCE_NAME to "adaptiveproduct_youtube_2024_q4_background_color_108",
            ADAPTIVE_FOREGROUND_RESOURCE_NAME to "adaptiveproduct_youtube_2024_q4_foreground_color_108",
        ).forEach { (old, new) ->
            val oldFile = targetDirectory.resolve("$old.png")
            if (oldFile.exists()) {
                val newFile = targetDirectory.resolve("$new.png")
                Files.write(newFile.toPath(), oldFile.readBytes())
            }
        }
    }
}
