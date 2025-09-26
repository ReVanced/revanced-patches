package app.revanced.patches.youtube.layout.branding

import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.shared.layout.branding.mipmapDirectories
import app.revanced.patches.shared.layout.branding.youtubeIconResourceFileNames
import app.revanced.patches.shared.layout.branding.youtubeIconResourceFileNamesNew
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import java.nio.file.Files

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
    iconResourceFileNames = youtubeIconResourceFileNames,
    resourceFolder = "custom-branding/youtube",

    block = {
        dependsOn(versionCheckPatch)

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
        if (is_19_34_or_greater) {
            val resourceDirectory = get("res")

            mipmapDirectories.forEach { directory ->
                val targetDirectory = resourceDirectory.resolve(directory)

                youtubeIconResourceFileNamesNew.forEach { (old, new) ->
                    val oldFile = targetDirectory.resolve("$old.png")
                    val newFile = targetDirectory.resolve("$new.png")

                    Files.write(newFile.toPath(), oldFile.readBytes())
                }
            }
        }
    }
)
