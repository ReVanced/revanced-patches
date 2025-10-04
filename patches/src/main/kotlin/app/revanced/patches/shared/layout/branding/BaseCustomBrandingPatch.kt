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
import app.revanced.util.findElementByAttributeValueOrThrow
import java.io.File
import java.nio.file.Files

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
 * Shared custom branding patch for YouTube and YT Music.
 */
internal fun baseCustomBrandingPatch(
    defaultAppName: String,
    appNameValues: Map<String, String>,
    patchResourceFolder: String,
    adaptiveAnyDpiFileNames: Array<String>,
    adaptiveMipmapFileNames: Array<String>,
    legacyMipmapFileNames: Array<String>,
    monochromeFileNames: Array<String>,
    manifestAppLauncherValue: String,
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
            ${formatResourceFileList((adaptiveMipmapFileNames + legacyMipmapFileNames))}
            
            Optionally, the path can contain a 'drawable' folder with the monochrome icon files:
            ${formatResourceFileList(monochromeFileNames)}
        """.trimIndentMultiline(),
    )

    block()

    dependsOn(
        // Change the app name.
        resourcePatch {
            execute {
                document("AndroidManifest.xml").use { document ->
                    document.childNodes.findElementByAttributeValueOrThrow(
                        "android:label",
                        manifestAppLauncherValue
                    ).nodeValue = appName!!
                }
            }
        }
    )

    execute {
        val iconPathTrimmed = iconPath!!.trim()

        if (iconPathTrimmed == REVANCED_ICON) {
            // Copy adaptive icons.
            copyResources(
                patchResourceFolder,
                ResourceGroup("mipmap-anydpi", *adaptiveAnyDpiFileNames)
            )

            // Copy legacy icons.
            mipmapDirectories.map { directory ->
                ResourceGroup(
                    directory,
                    *legacyMipmapFileNames,
                )
            }.forEach { groupResources ->
                copyResources(patchResourceFolder, groupResources)
            }

            // Copy monochrome icons.
            copyResources(
                patchResourceFolder,
                ResourceGroup("drawable", *monochromeFileNames)
            )
        } else {
            val iconPathFile = File(iconPathTrimmed)
            if (!iconPathFile.exists()) {
                throw PatchException("The custom icon path cannot be found: " +
                        iconPathFile.absolutePath
                )
            }

            if (!iconPathFile.isDirectory) {
                throw PatchException("The custom icon path must be a folder: "
                        + iconPathFile.absolutePath)
            }

            val resourceDirectory = get("res")
            var replacedResources = false

            // Replace mipmap icons.
            mipmapDirectories.map { directory ->
                ResourceGroup(
                    directory,
                    *adaptiveMipmapFileNames,
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
            monochromeFileNames.forEach { iconFileName ->
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
                throw PatchException("Could not find any replacement images in " +
                        "patch option path: " + iconPathFile.absolutePath)
            }
        }

        executeBlock() // Must be after the main code to rename the new icons for YouTube 19.34+.
    }
}
