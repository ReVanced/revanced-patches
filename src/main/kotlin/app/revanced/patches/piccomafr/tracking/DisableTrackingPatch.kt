package app.revanced.patches.piccomafr.tracking

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.piccomafr.tracking.fingerprints.AppMesurementFingerprint
import app.revanced.patches.piccomafr.tracking.fingerprints.FacebookSDKFingerprint
import app.revanced.patches.piccomafr.tracking.fingerprints.FirebaseInstallFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Disable tracking",
    description = "Disables tracking by replacing tracking URLs with example.com.",
    compatiblePackages = [
        CompatiblePackage(
            "com.piccomaeurope.fr",
            [
                "6.4.0",
                "6.4.1",
                "6.4.2",
                "6.4.3",
                "6.4.4",
                "6.4.5",
                "6.5.0",
                "6.5.1",
                "6.5.2",
                "6.5.3",
                "6.5.4",
                "6.6.0",
                "6.6.1",
                "6.6.2",
            ],
        ),
    ],
)
@Suppress("unused")
object DisableTrackingPatch : BytecodePatch(
    setOf(FacebookSDKFingerprint, FirebaseInstallFingerprint, AppMesurementFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        FacebookSDKFingerprint.result?.mutableMethod?.apply {
            getInstructions().filter { instruction ->
                instruction.opcode == Opcode.CONST_STRING
            }.forEach { instruction ->
                instruction as OneRegisterInstruction

                replaceInstruction(
                    instruction.location.index,
                    "const-string v${instruction.registerA}, \"example.com\"",
                )
            }
        } ?: throw FacebookSDKFingerprint.exception

        FirebaseInstallFingerprint.result?.mutableMethod?.apply {
            getInstructions().filter {
                it.opcode == Opcode.CONST_STRING
            }.filter {
                it.getReference<StringReference>()?.string == "firebaseinstallations.googleapis.com"
            }.forEach { instruction ->
                instruction as OneRegisterInstruction

                replaceInstruction(
                    instruction.location.index,
                    "const-string v${instruction.registerA}, \"example.com\"",
                )
            }
        } ?: throw FirebaseInstallFingerprint.exception

        AppMesurementFingerprint.result?.mutableMethod?.addInstruction(0, "return-void")
            ?: throw AppMesurementFingerprint.exception
    }
}
