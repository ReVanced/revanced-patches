package app.revanced.patches.youtube.layout.branding

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import java.io.File
import java.nio.file.Files

private const val REVANCED_ICON = "ReVanced*Logo" // Can never be a valid path.
private const val APP_NAME = "YouTube ReVanced"

private val iconResourceFileNames = arrayOf(
    "adaptiveproduct_youtube_background_color_108",
    "adaptiveproduct_youtube_foreground_color_108",
    "ic_launcher",
    "ic_launcher_round",
).map { "$it.png" }.toTypedArray()

private val iconResourceFileNamesNew = mapOf(
    "adaptiveproduct_youtube_foreground_color_108" to "adaptiveproduct_youtube_2024_q4_foreground_color_108",
    "adaptiveproduct_youtube_background_color_108" to "adaptiveproduct_youtube_2024_q4_background_color_108",
)

private val mipmapDirectories = arrayOf(
    "xxxhdpi",
    "xxhdpi",
    "xhdpi",
    "hdpi",
    "mdpi",
).map { "mipmap-$it" }

@Suppress("unused")
val customBrandingPatch = resourcePatch(
    name = "Custom branding",
    description = "Applies a custom app name and icon. Defaults to \"YouTube ReVanced\" and the ReVanced logo.",
    use = false,
) {
    dependsOn(versionCheckPatch)

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    val appName by stringOption(
        key = "appName",
        default = APP_NAME,
        values = mapOf(
            "YouTube ReVanced" to APP_NAME,
            "YT ReVanced" to "YT ReVanced",
            "YT" to "YT",
            "YouTube" to "YouTube",
        ),
        title = "App name",
        description = "The name of the app.",
    )

    val icon by stringOption(
        key = "iconPath",
        default = REVANCED_ICON,
        values = mapOf("ReVanced Logo" to REVANCED_ICON),
        title = "App icon",
        description = """
            The icon to apply to the app.
            
            If a path to a folder is provided, the folder must contain the following folders:

            ${mipmapDirectories.joinToString("\n") { "- $it" }}

            Each of these folders must contain the following files:

            ${iconResourceFileNames.joinToString("\n") { "- $it" }}
        """.trimIndentMultiline(),
    )

    execute {
        icon?.let { icon ->
            // Change the app icon.
            mipmapDirectories.map { directory ->
                ResourceGroup(
                    directory,
                    *iconResourceFileNames,
                )
            }.let { resourceGroups ->
                if (icon != REVANCED_ICON) {
                    val path = File(icon)
                    val resourceDirectory = get("res")

                    resourceGroups.forEach { group ->
                        val fromDirectory = path.resolve(group.resourceDirectoryName)
                        val toDirectory = resourceDirectory.resolve(group.resourceDirectoryName)

                        group.resources.forEach { iconFileName ->
                            Files.write(
                                toDirectory.resolve(iconFileName).toPath(),
                                fromDirectory.resolve(iconFileName).readBytes(),
                            )
                        }
                    }
                } else {
                    resourceGroups.forEach { copyResources("custom-branding", it) }
                }
            }

            if (is_19_34_or_greater) {
                val resourceDirectory = get("res")

                mipmapDirectories.forEach { directory ->
                    val targetDirectory = resourceDirectory.resolve(directory)

                    iconResourceFileNamesNew.forEach { (old, new) ->
                        val oldFile = targetDirectory.resolve("$old.png")
                        val newFile = targetDirectory.resolve("$new.png")

                        Files.write(newFile.toPath(), oldFile.readBytes())
                    }
                }
            }
        }

        appName?.let { name ->
            // Change the app name.
            val manifest = get("AndroidManifest.xml")
            manifest.writeText(
                manifest.readText()
                    .replace(
                        "android:label=\"@string/application_name",
                        "android:label=\"$name",
                    ),
            )
        }
    }
}
