package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen.fingerprints.CreateEndScreenViewFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Disable suggested video end screen",
    description = "Disables the suggested video end screen at the end of a video.",
    dependencies = [IntegrationsPatch::class, DisableSuggestedVideoEndScreenResourcePatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41"
            ]
        )
    ]
)
@Suppress("unused")
object DisableSuggestedVideoEndScreenPatch : BytecodePatch(
    setOf(CreateEndScreenViewFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/DisableSuggestedVideoEndScreenPatch;"

    override fun execute(context: BytecodeContext) {
        CreateEndScreenViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val addOnClickEventListenerIndex = it.scanResult.patternScanResult!!.endIndex - 1
                val viewRegister = getInstruction<FiveRegisterInstruction>(addOnClickEventListenerIndex).registerC

                addInstruction(
                    addOnClickEventListenerIndex + 1,
                    "invoke-static {v$viewRegister}, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->closeEndScreen(Landroid/widget/ImageView;)V"
                )
            }
        } ?: throw CreateEndScreenViewFingerprint.exception
    }
}
