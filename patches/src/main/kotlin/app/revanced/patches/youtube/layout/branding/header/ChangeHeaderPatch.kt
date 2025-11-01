package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.Document
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.layout.branding.addBrandLicensePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.forEachLiteralValueInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import java.io.File

private val variants = arrayOf("light", "dark")

private val targetResourceDirectoryNames = mapOf(
    "drawable-hdpi" to "194x72 px",
    "drawable-xhdpi" to "258x96 px",
    "drawable-xxhdpi" to "387x144 px",
    "drawable-xxxhdpi" to "512x192 px"
)

/**
 * Header logos built into this patch.
 */
private val logoResourceNames = arrayOf(
    "revanced_header_minimal",
    "revanced_header_rounded",
)

/**
 * Custom header resource/file name.
 */
private const val CUSTOM_HEADER_RESOURCE_NAME = "revanced_header_custom"

/**
 * Custom header resource/file names.
 */
private val customHeaderResourceFileNames = variants.map { variant ->
    "${CUSTOM_HEADER_RESOURCE_NAME}_$variant.png"
}.toTypedArray()

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ChangeHeaderPatch;"

private val changeHeaderBytecodePatch = bytecodePatch {
    dependsOn(
        resourceMappingPatch,
        addBrandLicensePatch
    )

    execute {
        // Verify images exist. Resources are not used during patching but extension code does.
        arrayOf(
            "yt_ringo2_wordmark_header",
            "yt_ringo2_premium_wordmark_header"
        ).forEach { resource ->
            variants.forEach { theme ->
                resourceMappings["drawable", resource + "_" + theme]
            }
        }

        arrayOf(
            "ytWordmarkHeader",
            "ytPremiumWordmarkHeader"
        ).forEach { resourceName ->
            val resourceId = resourceMappings["attr", resourceName]

            forEachLiteralValueInstruction(resourceId) { literalIndex ->
                val register = getInstruction<OneRegisterInstruction>(literalIndex).registerA
                addInstructions(
                    literalIndex + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getHeaderAttributeId(I)I
                        move-result v$register    
                    """
                )
            }
        }
    }
}

@Suppress("unused")
val changeHeaderPatch = resourcePatch(
    name = "Change header",
    description = "Adds an option to change the header logo in the top left corner of the app.",
) {
    dependsOn(addResourcesPatch, changeHeaderBytecodePatch)

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    val custom by stringOption(
        key = "custom",
        title = "Custom header logo",
        description = """
            Folder with images to use as a custom header logo.
            
            The folder must contain one or more of the following folders, depending on the DPI of the device:
            ${targetResourceDirectoryNames.keys.joinToString("\n") { "- $it" }}
            
            Each of the folders must contain all of the following files:
            ${customHeaderResourceFileNames.joinToString("\n")} 

            The image dimensions must be as follows:
            ${targetResourceDirectoryNames.map { (dpi, dim) -> "- $dpi: $dim" }.joinToString("\n")}
        """.trimIndentMultiline()
    )

    execute {
        addResources("youtube", "layout.branding.changeHeaderPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            if (custom == null) {
                ListPreference("revanced_header_logo")
            } else {
                ListPreference(
                    key = "revanced_header_logo",
                    entriesKey = "revanced_header_logo_custom_entries",
                    entryValuesKey = "revanced_header_logo_custom_entry_values"
                )
            }
        )

        logoResourceNames.forEach { logo ->
            variants.forEach { variant ->
                copyResources(
                    "change-header",
                    ResourceGroup(
                        "drawable",
                        logo + "_" + variant + ".xml"
                    )
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
                        *customHeaderResourceFileNames
                    )
                )
            }
        }

        // Logo is replaced using an attribute reference.
        document("res/values/attrs.xml").use { document ->
            val resources = document.childNodes.item(0)

            fun addAttributeReference(logoName: String) {
                val item = document.createElement("attr")
                item.setAttribute("format", "reference")
                item.setAttribute("name", logoName)
                resources.appendChild(item)
            }

            logoResourceNames.forEach { logoName ->
                addAttributeReference(logoName)
            }

            addAttributeReference(CUSTOM_HEADER_RESOURCE_NAME)
        }

        // Add custom drawables to all styles that use the regular and premium logo.
        document("res/values/styles.xml").use { document ->
            arrayOf(
                "Base.Theme.YouTube.Light" to "light",
                "Base.Theme.YouTube.Dark" to "dark",
                "CairoLightThemeRingo2Updates" to "light",
                "CairoDarkThemeRingo2Updates" to "dark"
            ).forEach { (style, mode) ->
                val styleElement = document.childNodes.findElementByAttributeValueOrThrow(
                    "name", style
                )

                fun addDrawableElement(document: Document, logoName: String, mode: String) {
                    val item = document.createElement("item")
                    item.setAttribute("name", logoName)
                    item.textContent = "@drawable/${logoName}_$mode"
                    styleElement.appendChild(item)
                }

                logoResourceNames.forEach { logoName ->
                    addDrawableElement(document, logoName, mode)
                }

                addDrawableElement(document, CUSTOM_HEADER_RESOURCE_NAME, mode)
            }
        }

        // Copy user provided images last, so if an exception is thrown due to bad input.
        if (custom != null) {
            val customFile = File(custom!!.trim())
            if (!customFile.exists()) {
                throw PatchException("The custom header path cannot be found: " +
                        customFile.absolutePath
                )
            }

            if (!customFile.isDirectory) {
                throw PatchException("The custom header path must be a folder: "
                        + customFile.absolutePath)
            }

            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            customFile.listFiles {
                file -> file.isDirectory && file.name in targetResourceDirectoryNames
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
                    throw PatchException("Both light/dark mode images " +
                            "must be specified but only found: " + customFiles.map { it.name })
                }

                customFiles.forEach { imgSourceFile ->
                    val imgTargetFile = targetDpiFolder.resolve(imgSourceFile.name)
                    imgSourceFile.copyTo(target = imgTargetFile, overwrite = true)

                    copiedFiles = true
                }
            }

            if (!copiedFiles) {
                throw PatchException("Expected to find directories and files: "
                        + customHeaderResourceFileNames.contentToString()
                        + "\nBut none were found in the provided option file path: " + customFile.absolutePath)
            }
        }
    }
}
