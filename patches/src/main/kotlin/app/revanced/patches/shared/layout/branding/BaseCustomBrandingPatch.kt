package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.patch.ResourcePatchBuilder
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import java.io.File
import java.nio.file.Files

private const val REVANCED_ICON = "ReVanced*Logo" // Can never be a valid path.

internal val mipmapDirectories = arrayOf(
    "xxxhdpi",
    "xxhdpi",
    "xhdpi",
    "hdpi",
    "mdpi",
).map { "mipmap-$it" }

/**
 * App name option for branding patches.
 */
internal fun appNameOption(
    defaultAppName: String,
    appNameValues: Map<String, String>
) = stringOption(
    key = "appName",
    default = defaultAppName,
    values = appNameValues,
    title = "App name",
    description = "The name of the app.",
)

/**
 * App icon option for branding patches.
 */
internal fun appIconOption(iconResourceFileNames: Array<String>) = stringOption(
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

/**
 * Attempts to fix unescaped and invalid characters not allowed for an Android app name.
 */
private fun escapedAppName(name: String?): String? {
    if (name == null) return null

    // Remove ASCII control characters.
    val cleanedName = name.filter { it.code >= 32 }

    // Replace invalid XML characters with escaped equivalents.
    val escapedName = cleanedName
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    return escapedName.ifBlank { null }
}

/**
 * Shared custom branding patch for YouTube and YT Music.
 */
internal fun baseCustomBrandingPatch(
    defaultAppName: String,
    appNameValues: Map<String, String>,
    iconResourceFileNames: Array<String>,
    resourceFolder: String,
    block: ResourcePatchBuilder.() -> Unit = {},
    executeBlock: ResourcePatchContext.() -> Unit = {}
) = resourcePatch(
    name = "Custom branding",
    description = "Applies a custom app name and icon. Defaults to \"$defaultAppName\" and the ReVanced logo.",
    use = false,
) {
    val iconResourceFileNamesPng = iconResourceFileNames.map { "$it.png" }.toTypedArray<String>()

    val appNameOption = appNameOption(defaultAppName, appNameValues)
    val appIconOption = appIconOption(iconResourceFileNamesPng)

    appNameOption()
    appIconOption()

    block()

    execute {
        val appName by appNameOption
        val icon by appIconOption

        icon?.let { iconPath ->
            // Change the app icon.
            mipmapDirectories.map { directory ->
                ResourceGroup(
                    directory,
                    *iconResourceFileNamesPng,
                )
            }.let { resourceGroups ->
                if (icon != REVANCED_ICON) {
                    val path = File(iconPath)
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
                    resourceGroups.forEach { copyResources(resourceFolder, it) }
                }
            }
        }

        executeBlock() // Must be after the main code to rename the new icons for YouTube 19.34+.

        // Change the app name.
        escapedAppName(appName)?.let { escapedAppName ->
            val manifest = get("AndroidManifest.xml")
            manifest.writeText(
                manifest.readText().replace(
                    "android:label=\"@string/application_name",
                    "android:label=\"$escapedAppName",
                )
            )
        }
    }
}
