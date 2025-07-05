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

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ChangeHeaderPatch;"

private val changeHeaderBytecodePatch = bytecodePatch {
    dependsOn(resourceMappingPatch)

    execute {
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

private val targetResourceDirectoryNames = mapOf(
    "xxxhdpi" to "512px x 192px",
    "xxhdpi" to "387px x 144px",
    "xhdpi" to "258px x 96px",
    "hdpi" to "194px x 72px",
    "mdpi" to "129px x 48px"
).mapKeys { (dpi, _) -> "drawable-$dpi" }

private val variants = arrayOf("light", "dark")

/**
 * Header logos built into this patch.
 */
private val logoResourceNames = arrayOf(
    "revanced_header_logo_minimal",
    "revanced_header_logo",
)

/**
 * Custom header resource/file name.
 */
private const val CUSTOM_HEADER_RESOURCE_NAME = "custom_header"

@Suppress("unused")
val changeHeaderPatch = resourcePatch(
    name = "Change header",
    description = "Adds an option to change the header logo in the top left corner of the app.",
) {
    dependsOn(addResourcesPatch, changeHeaderBytecodePatch)

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
            "20.13.41",
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
            ${variants.joinToString("\n") { variant -> "- ${CUSTOM_HEADER_RESOURCE_NAME}_$variant.png" }}

            The image dimensions must be as follows:
            ${targetResourceDirectoryNames.map { (dpi, dim) -> "- $dpi: $dim" }.joinToString("\n")}
        """.trimIndentMultiline()
    )

    execute {
        addResources("youtube", "layout.branding.changeHeaderPatch")

        fun getLightDarkFileNames(vararg resourceNames: String): Array<String> =
            variants.flatMap { variant ->
                resourceNames.map { resource -> "${resource}_$variant.png" }
            }.toTypedArray()

        val logoResourceFileNames = getLightDarkFileNames(*logoResourceNames)
        copyResources(
            "change-header",
            ResourceGroup("drawable-hdpi", *logoResourceFileNames),
            ResourceGroup("drawable-mdpi", *logoResourceFileNames),
            ResourceGroup("drawable-xhdpi", *logoResourceFileNames),
            ResourceGroup("drawable-xxhdpi", *logoResourceFileNames),
            ResourceGroup("drawable-xxxhdpi", *logoResourceFileNames),
        )

        if (custom != null) {
            val sourceFolders = File(custom!!).listFiles { file -> file.isDirectory }
                ?: throw PatchException("The provided path is not a directory: $custom")

            val customResourceFileNames = getLightDarkFileNames(CUSTOM_HEADER_RESOURCE_NAME)

            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            sourceFolders.forEach { dpiSourceFolder ->
                val targetDpiFolder = get("res").resolve(dpiSourceFolder.name)
                if (!targetDpiFolder.exists()) return@forEach

                val customFiles = dpiSourceFolder.listFiles { file ->
                    file.isFile && file.name in customResourceFileNames
                }!!

                if (customFiles.size > 0 && customFiles.size != variants.size) {
                    throw PatchException("Both light/dark mode images " +
                                "must be specified but only found: " + customFiles.map { it.name })
                }

                customFiles.forEach { imgSourceFile ->
                    val imgTargetFile = targetDpiFolder.resolve(imgSourceFile.name)
                    imgSourceFile.copyTo(imgTargetFile)

                    copiedFiles = true
                }
            }

            if (!copiedFiles) {
                throw PatchException("No custom header images found in the provided path: $custom")
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

            if (custom != null) {
                addAttributeReference(CUSTOM_HEADER_RESOURCE_NAME)
            }
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

                if (custom != null) {
                    addDrawableElement(document, CUSTOM_HEADER_RESOURCE_NAME, mode)
                }
            }
        }

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
    }
}
