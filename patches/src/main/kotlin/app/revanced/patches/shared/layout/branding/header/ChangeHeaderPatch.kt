package app.revanced.patches.shared.layout.branding.header

import app.revanced.patcher.patch.Package
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import java.io.File

internal const val CUSTOM_HEADER_RESOURCE_NAME = "revanced_header_custom"

@Suppress("unused")
fun changeHeaderPatch(
    targetResourceDirectoryNames: Map<String, String>,
    changeHeaderBytecodePatch: Patch,
    vararg compatiblePackages: Package,
    variants: Array<String>,
    logoResourceNames: Array<String>,
    preferenceScreen: BasePreferenceScreen.Screen,
    resourcesAppId: String,
    applyBlock: ResourcePatchContext.() -> Unit = {},
): Patch {
    val customHeaderResourceFileNames = variants.map { variant ->
        "${CUSTOM_HEADER_RESOURCE_NAME}_$variant.png"
    }.toTypedArray()

    return resourcePatch(
        name = "Change header",
        description = "Adds an option to change the header logo in the top left corner of the app.",
    ) {
        dependsOn(addResourcesPatch, changeHeaderBytecodePatch)

        compatibleWith(packages = compatiblePackages)

        val custom by stringOption(
            name = "Custom header logo",
            description = """
            Folder with images to use as a custom header logo.
            
            The folder must contain one or more of the following folders, depending on the DPI of the device:
            ${targetResourceDirectoryNames.keys.joinToString("\n") { "- $it" }}
            
            Each of the folders must contain all of the following files:
            ${customHeaderResourceFileNames.joinToString("\n")} 

            The image dimensions must be as follows:
            ${targetResourceDirectoryNames.map { (dpi, dim) -> "- $dpi: $dim" }.joinToString("\n")}
        """.trimIndentMultiline(),
        )

        apply {
            addResources(resourcesAppId, "layout.branding.header.changeHeaderPatch")

            preferenceScreen.addPreferences(
                if (custom == null) {
                    ListPreference("revanced_header_logo")
                } else {
                    ListPreference(
                        key = "revanced_header_logo",
                        entriesKey = "revanced_header_logo_custom_entries",
                        entryValuesKey = "revanced_header_logo_custom_entry_values",
                    )
                },
            )

            logoResourceNames.forEach { logo ->
                variants.forEach { variant ->
                    copyResources(
                        "change-header",
                        ResourceGroup(
                            "drawable",
                            logo + "_" + variant + ".xml",
                        ),
                    )
                }
            }

            // Copy custom template. Images are only used if settings
            // are imported and a custom header is enabled.
            targetResourceDirectoryNames.keys.forEach { dpi ->
                variants.forEach { variant ->
                    copyResources(
                        "change-header",
                        ResourceGroup(
                            dpi,
                            resources = customHeaderResourceFileNames,
                        ),
                    )
                }
            }

            applyBlock()

            // Copy user provided images last, so if an exception is thrown due to bad input.
            if (custom != null) {
                val customFile = File(custom!!.trim())
                if (!customFile.exists()) {
                    throw PatchException(
                        "The custom header path cannot be found: " +
                                customFile.absolutePath,
                    )
                }

                if (!customFile.isDirectory) {
                    throw PatchException(
                        "The custom header path must be a folder: " +
                                customFile.absolutePath,
                    )
                }

                var copiedFiles = false

                // For each source folder, copy the files to the target resource directories.
                customFile.listFiles { file ->
                    file.isDirectory && file.name in targetResourceDirectoryNames
                }!!.forEach { dpiSourceFolder ->
                    val targetDpiFolder = get("res").resolve(dpiSourceFolder.name)
                    if (!targetDpiFolder.exists()) {
                        // Should never happen.
                        throw IllegalStateException("Resource not found: $dpiSourceFolder")
                    }

                    val customFiles = dpiSourceFolder.listFiles { file ->
                        file.isFile && file.name in customHeaderResourceFileNames
                    }!!

                    if (customFiles.isNotEmpty() && customFiles.size != variants.size) {
                        throw PatchException(
                            "Both light/dark mode images " +
                                    "must be specified but only found: " + customFiles.map { it.name },
                        )
                    }

                    customFiles.forEach { imgSourceFile ->
                        val imgTargetFile = targetDpiFolder.resolve(imgSourceFile.name)
                        imgSourceFile.copyTo(target = imgTargetFile, overwrite = true)

                        copiedFiles = true
                    }
                }

                if (!copiedFiles) {
                    throw PatchException(
                        "Expected to find directories and files: " +
                                customHeaderResourceFileNames.contentToString() +
                                "\nBut none were found in the provided option file path: " + customFile.absolutePath,
                    )
                }
            }
        }
    }
}