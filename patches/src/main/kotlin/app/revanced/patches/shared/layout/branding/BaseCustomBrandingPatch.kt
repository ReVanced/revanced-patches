package app.revanced.patches.shared.layout.branding

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
    "xxxhdpi",
    "xxhdpi",
    "xhdpi",
    "hdpi",
    "mdpi",
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
    launchScreenAnimationFileName: String? = null,
    block: ResourcePatchBuilder.() -> Unit = {},
    executeBlock: ResourcePatchContext.() -> Unit = {}
): ResourcePatch = resourcePatch(
    name = "Custom branding",
    description = "Applies a custom app name and icon. Defaults to \"$defaultAppName\" and the ReVanced logo.",
    use = false,
) {
    val iconResourceFileNamesPng = iconResourceFileNames.map { "$it.png" }.toTypedArray<String>()

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
            
            If a path to a folder is provided, the folder must contain the following folders:
    
            ${formatResourceFileList(mipmapDirectories)}
    
            Each of these folders must contain the following files:
    
            ${formatResourceFileList(iconResourceFileNamesPng)}
        """.trimIndentMultiline(),
    )

    block()

    execute {
        // Change the app icon and launch screen.
        val iconResourceGroups = mipmapDirectories.map { directory ->
            ResourceGroup(
                directory,
                *iconResourceFileNamesPng,
            )
        }

        val iconPathTrimmed = iconPath!!.trim()
        if (iconPathTrimmed == REVANCED_ICON) {
            iconResourceGroups.forEach {
                copyResources(resourceFolder, it)
            }
        } else {
            val filePath = File(iconPathTrimmed)
            val resourceDirectory = get("res")

            iconResourceGroups.forEach { group ->
                val fromDirectory = filePath.resolve(group.resourceDirectoryName)
                val toDirectory = resourceDirectory.resolve(group.resourceDirectoryName)

                group.resources.forEach { iconFileName ->
                    Files.write(
                        toDirectory.resolve(iconFileName).toPath(),
                        fromDirectory.resolve(iconFileName).readBytes(),
                    )
                }
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

        if (launchScreenAnimationFileName != null) {
            copyResources(
                resourceFolder,
                ResourceGroup(
                    "raw",
                    launchScreenAnimationFileName
                )
            )
        }

        executeBlock() // Must be after the main code to rename the new icons for YouTube 19.34+.
    }
}
