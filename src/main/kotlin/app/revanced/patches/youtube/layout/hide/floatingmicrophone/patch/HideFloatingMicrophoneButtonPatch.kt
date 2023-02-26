package app.revanced.patches.youtube.layout.hide.floatingmicrophone.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.annotations.HideFloatingMicrophoneButtonCompatibility
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints.ShowFloatingMicrophoneButtonFingerprint
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@Name("hide-floating-microphone-button")
@Description("Hides the floating microphone button which appears in search.")
@DependsOn([HideFloatingMicrophoneButtonResourcePatch::class])
@HideFloatingMicrophoneButtonCompatibility
@Version("0.0.1")
class HideFloatingMicrophoneButtonPatch : BytecodePatch(
    listOf(ShowFloatingMicrophoneButtonFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ShowFloatingMicrophoneButtonFingerprint.result?.let { result ->
            with(result.mutableMethod) {
                val insertIndex = result.scanResult.patternScanResult!!.startIndex + 1
                val showButtonRegister = (instruction(insertIndex - 1) as TwoRegisterInstruction).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static {v$showButtonRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->hideFloatingMicrophoneButton(Z)Z
                        move-result v$showButtonRegister
                        """
                )
            }
        } ?: return ShowFloatingMicrophoneButtonFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/HideFloatingMicrophoneButtonPatch;"
    }
}
