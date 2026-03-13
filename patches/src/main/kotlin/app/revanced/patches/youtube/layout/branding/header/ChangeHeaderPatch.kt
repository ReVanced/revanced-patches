package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.wideLiteral
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.Document
import app.revanced.patches.shared.layout.branding.addBrandLicensePatch
import app.revanced.patches.shared.layout.branding.header.CUSTOM_HEADER_RESOURCE_NAME
import app.revanced.patches.shared.layout.branding.header.changeHeaderPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal val variants = arrayOf("light", "dark")

private val logoResourceNames = arrayOf(
    "revanced_header_minimal",
    "revanced_header_rounded",
)

private val targetResourceDirectoryNames = mapOf(
    "drawable-hdpi" to "194x72 px",
    "drawable-xhdpi" to "258x96 px",
    "drawable-xxhdpi" to "387x144 px",
    "drawable-xxxhdpi" to "512x192 px",
)


private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ChangeHeaderPatch;"

private val changeHeaderBytecodePatch = bytecodePatch {
    dependsOn(
        resourceMappingPatch,
        addBrandLicensePatch,
    )

    apply {
        // Verify images exist. Resources are not used during patching but extension code does.
        arrayOf(
            "yt_ringo2_wordmark_header",
            "yt_ringo2_premium_wordmark_header",
        ).forEach { resource ->
            variants.forEach { theme ->
                ResourceType.DRAWABLE[resource + "_" + theme]
            }
        }

        arrayOf(
            "ytWordmarkHeader",
            "ytPremiumWordmarkHeader",
        ).forEach { resourceName ->
            val id = ResourceType.ATTR[resourceName]

            forEachInstructionAsSequence({ _, method, instruction, index ->
                if (instruction.wideLiteral != id) return@forEachInstructionAsSequence null

                val register = method.getInstruction<OneRegisterInstruction>(index).registerA

                return@forEachInstructionAsSequence index to register
            }) { method, (index, register) ->
                method.addInstructions(
                    index + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getHeaderAttributeId(I)I
                        move-result v$register    
                    """,
                )
            }
        }
    }
}


val changeHeaderPatch = changeHeaderPatch(
    targetResourceDirectoryNames = targetResourceDirectoryNames,
    changeHeaderBytecodePatch = changeHeaderBytecodePatch,
    compatiblePackages = arrayOf(
        "com.google.android.youtube" to setOf(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45",
            "20.44.38"
        )
    ),
    variants = variants,
    logoResourceNames = logoResourceNames,
    preferenceScreen = PreferenceScreen.GENERAL,
    resourcesAppId = "youtube",
) {
    // Logo is replaced using an attribute reference.
    document("res/values/attrs.xml").use { document ->
        val resources = document.childNodes.item(0)

        fun addAttributeReference(logoName: String) {
            val item = document.createElement("attr")
            item.setAttribute("format", "reference")
            item.setAttribute("name", logoName)
            resources.appendChild(item)
        }

        logoResourceNames.forEach { logoName -> addAttributeReference(logoName) }

        addAttributeReference(CUSTOM_HEADER_RESOURCE_NAME)
    }

    // Add custom drawables to all styles that use the regular and premium logo.
    document("res/values/styles.xml").use { document ->
        arrayOf(
            "Base.Theme.YouTube.Light" to "light",
            "Base.Theme.YouTube.Dark" to "dark",
            "CairoLightThemeRingo2Updates" to "light",
            "CairoDarkThemeRingo2Updates" to "dark",
        ).forEach { (style, mode) ->
            val styleElement = document.childNodes.findElementByAttributeValueOrThrow("name", style)

            fun addDrawableElement(document: Document, logoName: String, mode: String) {
                val item = document.createElement("item")
                item.setAttribute("name", logoName)
                item.textContent = "@drawable/${logoName}_$mode"
                styleElement.appendChild(item)
            }

            logoResourceNames.forEach { logoName -> addDrawableElement(document, logoName, mode) }

            addDrawableElement(document, CUSTOM_HEADER_RESOURCE_NAME, mode)
        }
    }
}