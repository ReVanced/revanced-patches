package app.revanced.patches.youtube.overlaybutton.download.hook

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.overlaybutton.download.hook.fingerprints.DownloadActionsFingerprint
import app.revanced.patches.youtube.overlaybutton.download.hook.fingerprints.DownloadActionsCommandFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.Opcode

object DownloadButtonHookPatch : BytecodePatch(
    setOf(
        DownloadActionsFingerprint,
        DownloadActionsCommandFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        // Remove the Default Download dialog of YouTube (called before loading videoId)
        DownloadActionsFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.startIndex

                addInstructionsWithLabels(
                    targetIndex, """
                        invoke-static {}, $UTILS_PATH/HookDownloadButtonPatch;->shouldHookDownloadButton()Z
                        move-result v0
                        if-eqz v0, :default
                        return-void
                        """, ExternalLabel("default", getInstruction(targetIndex))
                )
            }
        } ?: throw DownloadActionsFingerprint.exception

        // Get videoId and startDownloadActivity
        DownloadActionsCommandFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertMethod = it.mutableClass.methods.find { method -> method.name == "run" }
                
                insertMethod?.apply {
                    val targetIndex = insertMethod.implementation!!.instructions
                        .indexOfFirst { instruction -> instruction.opcode == Opcode.IF_NE }

                    for (index in targetIndex until targetIndex + 12) {
                        if (getInstruction(index).opcode != Opcode.MOVE_OBJECT) continue

                        val register = getInstruction<OneRegisterInstruction>(index).registerA

                        val targetReference =
                            getInstruction<ReferenceInstruction>(index + 1).reference.toString()

                        if (targetReference != "Ljava/lang/String;") throw PatchException("Couldn't find insertIndex")

                        addInstruction(
                            index + 2,
                            "invoke-static {v$register}, $UTILS_PATH/HookDownloadButtonPatch;->startDownloadActivity(Ljava/lang/String;)V"
                        )
                    }
                } ?: throw PatchException("Failed to find Runnable method")
            }
        } ?: throw DownloadActionsCommandFingerprint.exception
    }
}
