package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.ResourcePatchBuilder
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import java.io.File
import java.nio.file.Files
import java.util.logging.Logger

private const val REVANCED_ICON = "ReVanced*Logo" // Can never be a valid path.

internal val mipmapDirectories = arrayOf(
    // Target app does not have ldpi icons.
    "mdpi",
    "hdpi",
    "xhdpi",
    "xxhdpi",
    "xxxhdpi",
).map { "mipmap-$it" }.toTypedArray()

private fun formatResourceFileList(resourceNames: Array<String>) = resourceNames.joinToString("\n") { "- $it" }

/**
 * Attempts to fix unescaped and invalid characters not allowed for an Android app name.
 */
private fun escapeAppName(name: String): String? {
    // Remove ASCII control characters.
    val cleanedName = name.filter { it.code >= 32 }

    // Replace invalid XML characters with escaped equivalents.
    val escapedName = cleanedName
        .replace("&", "&amp;") // Must be first to avoid double-escaping.
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace(Regex("(?<!&)\""), "&quot;")

    // Trim empty spacing.
    val trimmed = escapedName.trim()

    return trimmed.ifBlank { null }
}

/**
 * Shared custom branding patch for YouTube and YT Music.
 */
internal fun baseCustomBrandingPatch(
    defaultAppName: String,
    appNameValues: Map<String, String>,
    resourceFolder: String,
    iconResourceFileNames: Array<String>,
    monochromeIconFileNames: Array<String>,
    adaptiveIconFileNames: Array<String>,
    legacyIconResourceFileNames: Array<String>,
    block: ResourcePatchBuilder.() -> Unit = {},
    executeBlock: ResourcePatchContext.() -> Unit = {}
): ResourcePatch = resourcePatch(
    name = "Custom branding",
    description = "Applies a custom app name and icon. Defaults to \"$defaultAppName\" and the ReVanced logo.",
    use = false,
) {
    val appName by stringOption(
        key = "appName",
        default = defaultAppName,
        values = appNameValues,
        title = "App name",
        description = "The name of the app.",
    )

    val iconPath by stringOption(
        key = "iconPath",
        default = REVANCED_ICON,
        values = mapOf("ReVanced Logo" to REVANCED_ICON),
        title = "App icon",
        description = """
            The icon to apply to the app.
            
            If a path to a folder is provided, the folder must contain one or more of the following folders:
            ${formatResourceFileList(mipmapDirectories)}
    
            Each of these folders must contain the following files:
            ${formatResourceFileList((iconResourceFileNames + legacyIconResourceFileNames))}
            
            Optionally, a 'drawable' folder with the monochrome icon files:
            ${formatResourceFileList(monochromeIconFileNames)}
        """.trimIndentMultiline(),
    )

    block()

    execute {
        val iconPathTrimmed = iconPath!!.trim()

        if (iconPathTrimmed == REVANCED_ICON) {
            // Copy adaptive icons.
            copyResources(
                resourceFolder,
                ResourceGroup("mipmap-anydpi", *adaptiveIconFileNames)
            )

            // Copy legacy icons.
            mipmapDirectories.map { directory ->
                ResourceGroup(
                    directory,
                    *legacyIconResourceFileNames,
                )
            }.forEach { groupResources ->
                copyResources(resourceFolder, groupResources)
            }

            // Copy monochrome icons.
            copyResources(
                resourceFolder,
                ResourceGroup("drawable", *monochromeIconFileNames)
            )
        } else {
            val iconPathFile = File(iconPathTrimmed)
            val resourceDirectory = get("res")
            var replacedResources = false

            // Replace mipmap icons.
            mipmapDirectories.map { directory ->
                ResourceGroup(
                    directory,
                    *iconResourceFileNames,
                )
            }.forEach { groupResources ->
                val groupResourceDirectoryName = groupResources.resourceDirectoryName
                val fromDirectory = iconPathFile.resolve(groupResourceDirectoryName)
                val toDirectory = resourceDirectory.resolve(groupResourceDirectoryName)

                groupResources.resources.forEach { iconFileName ->
                    val replacement = fromDirectory.resolve(iconFileName)
                    if (replacement.exists()) {
                        Files.write(
                            toDirectory.resolve(iconFileName).toPath(),
                            replacement.readBytes(),
                        )
                        replacedResources = true
                    }
                }
            }

            // Replace monochrome icons if provided.
            monochromeIconFileNames.forEach { iconFileName ->
                val resourceType = "drawable"
                val replacement = iconPathFile.resolve(resourceType).resolve(iconFileName)
                if (replacement.exists()) {
                    Files.write(
                        resourceDirectory.resolve(resourceType).resolve(iconFileName).toPath(),
                        replacement.readBytes(),
                    )
                    replacedResources = true
                }
            }

            if (!replacedResources) {
                throw PatchException("Could not find any replacement images in patch option path: $iconPathTrimmed")
            }
        }

        // Change the app name.
        escapeAppName(appName!!)?.let { escapedAppName ->
            val newValue = "android:label=\"$escapedAppName\""

            val manifest = get("AndroidManifest.xml")
            val original = manifest.readText()
            val replacement = original
                // YouTube
                .replace("android:label=\"@string/application_name\"", newValue)
                // YT Music
                .replace("android:label=\"@string/app_launcher_name\"", newValue)

            if (original == replacement) {
                Logger.getLogger(this::class.java.name).warning(
                    "Could not replace manifest app name"
                )
            }

            manifest.writeText(replacement)
        }

        executeBlock() // Must be after the main code to rename the new icons for YouTube 19.34+.
    }
}
