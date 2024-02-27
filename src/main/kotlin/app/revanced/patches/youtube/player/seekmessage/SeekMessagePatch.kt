package app.revanced.patches.youtube.player.seekmessage

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.player.seekmessage.fingerprints.SeekEduContainerFingerprint
import app.revanced.patches.youtube.player.seekmessage.fingerprints.SeekEduUndoOverlayFingerprint
import app.revanced.patches.youtube.utils.controlsoverlay.DisableControlsOverlayConfigPatch
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.SeekUndoEduOverlayStub
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide seek message",
    description = "Adds an option to hide the 'Slide left or right to seek' or 'Release to cancel' message container in the video player.",
    dependencies = [
        DisableControlsOverlayConfigPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
object SeekMessagePatch : BytecodePatch(
    setOf(
        SeekEduContainerFingerprint,
        SeekEduUndoOverlayFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        SeekEduContainerFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $PLAYER->hideSeekMessage()Z
                        move-result v0
                        if-eqz v0, :default
                        return-void
                        """, ExternalLabel("default", getInstruction(0))
                )
            }
        } ?: throw SeekEduContainerFingerprint.exception

        /**
         * Added in YouTube v18.29.xx~
         */
        SeekEduUndoOverlayFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val seekUndoCalls = implementation!!.instructions.withIndex()
                    .filter { instruction ->
                        (instruction.value as? WideLiteralInstruction)?.wideLiteral == SeekUndoEduOverlayStub
                    }
                val insertIndex = seekUndoCalls.elementAt(seekUndoCalls.size - 1).index
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                val jumpIndex = implementation!!.instructions.let {
                    insertIndex + it.subList(insertIndex, it.size - 1).indexOfFirst { instruction ->
                        instruction.opcode == Opcode.INVOKE_VIRTUAL
                                && ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.name == "setOnClickListener"
                    }
                }
                val constComponent = getConstComponent(insertIndex, jumpIndex - 1)

                addInstructionsWithLabels(
                    insertIndex, constComponent + """
                        invoke-static {}, $PLAYER->hideSeekUndoMessage()Z
                        move-result v$insertRegister
                        if-nez v$insertRegister, :default
                        """, ExternalLabel("default", getInstruction(jumpIndex + 1))
                )

                /**
                 * Add settings
                 */
                SettingsPatch.addPreference(
                    arrayOf(
                        "PREFERENCE: PLAYER_SETTINGS",
                        "SETTINGS: HIDE_SEEK_UNDO_MESSAGE"
                    )
                )
            }
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: HIDE_SEEK_MESSAGE"
            )
        )

        SettingsPatch.updatePatchStatus("Hide seek message")

    }

    private fun MutableMethod.getConstComponent(
        startIndex: Int,
        endIndex: Int
    ): String {
        val constRegister =
            getInstruction<FiveRegisterInstruction>(endIndex).registerE

        for (index in endIndex downTo startIndex) {
            if (getInstruction(index).opcode != Opcode.CONST_16)
                continue

            if (getInstruction<OneRegisterInstruction>(index).registerA != constRegister)
                continue

            val constValue = getInstruction<WideLiteralInstruction>(index).wideLiteral.toInt()

            return "const/16 v$constRegister, $constValue"
        }
        return ""
    }
}
