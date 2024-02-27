package app.revanced.patches.youtube.player.suggestedvideooverlay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.player.suggestedvideooverlay.fingerprints.CoreContainerBuilderFingerprint
import app.revanced.patches.youtube.player.suggestedvideooverlay.fingerprints.MainAppAutoNavFingerprint
import app.revanced.patches.youtube.player.suggestedvideooverlay.fingerprints.MainAppAutoNavParentFingerprint
import app.revanced.patches.youtube.player.suggestedvideooverlay.fingerprints.TouchAreaOnClickListenerFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.CoreContainer
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Hide suggested video overlay",
    description = "Adds an option to hide the suggested video overlay at the end of videos.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object SuggestedVideoOverlayPatch : BytecodePatch(
    setOf(
        CoreContainerBuilderFingerprint,
        MainAppAutoNavParentFingerprint,
        TouchAreaOnClickListenerFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        CoreContainerBuilderFingerprint.result?.let {
            it.mutableMethod.apply {
                val addOnClickEventListenerIndex = it.scanResult.patternScanResult!!.endIndex - 1
                val viewRegister = getInstruction<FiveRegisterInstruction>(addOnClickEventListenerIndex).registerC

                addInstruction(
                    addOnClickEventListenerIndex + 1,
                    "invoke-static {v$viewRegister}, $PLAYER->hideSuggestedVideoOverlay(Landroid/widget/ImageView;)V"
                )
            }
        } ?: throw CoreContainerBuilderFingerprint.exception

        TouchAreaOnClickListenerFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertMethod = it.mutableClass.methods.find { method -> method.parameters.size == 1 }
                
                insertMethod?.apply {            
                    val targetIndex = insertMethod.implementation!!.instructions
                        .indexOfFirst { instruction -> instruction.opcode == Opcode.INVOKE_VIRTUAL }
                    val targetRegister = getInstruction<Instruction35c>(targetIndex).registerC

                    addInstruction(
                        targetIndex + 1,
                        "invoke-static {v$targetRegister}, $PLAYER->hideSuggestedVideoOverlay(Landroid/view/View;)V"
                    )
                } ?: throw PatchException("Failed to find onClick method")

            }
        } ?: throw TouchAreaOnClickListenerFingerprint.exception

        MainAppAutoNavParentFingerprint.result?.mutableClass?.let { mutableClass ->
            MainAppAutoNavFingerprint.also { it.resolve(context, mutableClass) }.result?.let {
                it.mutableMethod.apply {
                    val targetIndex = implementation!!.instructions.size - 1
                    val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstruction(
                        targetIndex,
                        "invoke-static {v$targetRegister}, $PLAYER->saveAutoplay(Z)V"
                    )
                }
            } ?: throw MainAppAutoNavFingerprint.exception
        } ?: throw MainAppAutoNavParentFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: HIDE_SUGGESTED_VIDEO_OVERLAY"
            )
        )

        SettingsPatch.updatePatchStatus("Hide suggested video overlay")

    }
}
