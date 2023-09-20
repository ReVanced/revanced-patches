package app.revanced.patches.youtube.layout.hide.floatingmicrophone

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints.ShowFloatingMicrophoneButtonFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide floating microphone button",
    description = "Hides the floating microphone button which appears in search.",
    dependencies = [HideFloatingMicrophoneButtonResourcePatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideFloatingMicrophoneButtonPatch : BytecodePatch(
    setOf(ShowFloatingMicrophoneButtonFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/HideFloatingMicrophoneButtonPatch;"

    override fun execute(context: BytecodeContext) {
        ShowFloatingMicrophoneButtonFingerprint.result?.let { result ->
            with(result.mutableMethod) {
                val insertIndex = result.scanResult.patternScanResult!!.startIndex + 1
                val showButtonRegister =
                    getInstruction<TwoRegisterInstruction>(insertIndex - 1).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static {v$showButtonRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->hideFloatingMicrophoneButton(Z)Z
                        move-result v$showButtonRegister
                        """
                )
            }
        } ?: throw ShowFloatingMicrophoneButtonFingerprint.exception
    }
}
