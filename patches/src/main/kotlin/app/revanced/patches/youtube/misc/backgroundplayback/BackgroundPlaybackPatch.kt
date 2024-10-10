package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findOpcodeIndicesReversed
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var prefBackgroundAndOfflineCategoryId = -1L
    private set

private val backgroundPlaybackResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        prefBackgroundAndOfflineCategoryId = resourceMappings["string", "pref_background_and_offline_category"]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/BackgroundPlaybackPatch;"

@Suppress("unused")
val backgroundPlaybackPatch = bytecodePatch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {
    dependsOn(
        backgroundPlaybackResourcePatch,
        sharedExtensionPatch,
        playerTypeHookPatch,
        videoInformationPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
        ),
    )

    val backgroundPlaybackManagerMatch by backgroundPlaybackManagerFingerprint()
    val backgroundPlaybackSettingsMatch by backgroundPlaybackSettingsFingerprint()
    val kidsBackgroundPlaybackPolicyControllerMatch by kidsBackgroundPlaybackPolicyControllerFingerprint()

    execute { context ->
        backgroundPlaybackManagerMatch.mutableMethod.apply {
            findOpcodeIndicesReversed(Opcode.RETURN).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructionsAtControlFlowLabel(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->allowBackgroundPlayback(Z)Z
                        move-result v$register 
                    """,
                )
            }
        }

        // Enable background playback option in YouTube settings
        backgroundPlaybackSettingsMatch.mutableMethod.apply {
            val booleanCalls = instructions.withIndex()
                .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

            val settingsBooleanIndex = booleanCalls.elementAt(1).index
            val settingsBooleanMethod = context.navigate(this).at(settingsBooleanIndex).mutable()

            settingsBooleanMethod.addInstructions(
                0,
                """
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->overrideBackgroundPlaybackAvailable()Z
                    move-result v0
                    return v0
                """,
            )
        }

        // Force allowing background play for videos labeled for kids.
        kidsBackgroundPlaybackPolicyControllerMatch.mutableMethod.addInstruction(
            0,
            "return-void",
        )
    }
}
