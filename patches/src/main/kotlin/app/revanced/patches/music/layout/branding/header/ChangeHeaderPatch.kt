package app.revanced.patches.music.layout.branding.header

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.wideLiteral
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.shared.layout.branding.header.changeHeaderPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private val targetResourceDirectoryNames = mapOf(
    "drawable-hdpi" to "121x36 px",
    "drawable-xhdpi" to "160x48 px",
    "drawable-xxhdpi" to "240x72 px",
    "drawable-xxxhdpi" to "320x96 px"
)

private val variants = arrayOf("dark")
private val logoResourceNames = arrayOf("revanced_header_dark")

private val headerDrawableNames = arrayOf(
    "action_bar_logo_ringo2",
    "ytm_logo_ringo2"
)

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/music/patches/ChangeHeaderPatch;"

private val changeHeaderBytecodePatch = bytecodePatch {
    dependsOn(resourceMappingPatch)

    apply {
        headerDrawableNames.forEach { drawableName ->
            val drawableId = ResourceType.DRAWABLE[drawableName]

            forEachInstructionAsSequence({ _, method, instruction, index ->
                if (instruction.wideLiteral != drawableId) return@forEachInstructionAsSequence null

                val register = method.getInstruction<OneRegisterInstruction>(index).registerA

                return@forEachInstructionAsSequence index to register
            }) { method, (index, register) ->
                method.addInstructions(
                    index + 1,
                    """
                        invoke-static { v$register }, ${EXTENSION_CLASS_DESCRIPTOR}->getHeaderDrawableId(I)I
                        move-result v$register    
                    """,
                )
            }
        }
    }
}

@Suppress("unused")
val changeHeaderPatch = changeHeaderPatch(
    targetResourceDirectoryNames = targetResourceDirectoryNames,
    changeHeaderBytecodePatch = changeHeaderBytecodePatch,
    logoResourceNames = logoResourceNames,
    variants = variants,
    preferenceScreen = PreferenceScreen.GENERAL,
    compatiblePackages = arrayOf(
        "com.google.android.apps.youtube.music" to setOf(
            "7.29.52",
            "8.10.52",
            "8.37.56",
            "8.40.54",
            "8.44.54"
        ),
    ),
    resourcesAppId = "music",
)
