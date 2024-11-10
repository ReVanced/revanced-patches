package app.revanced.patches.piccomafr.tracking

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val disableTrackingPatch = bytecodePatch(
    name = "Disable tracking",
    description = "Disables tracking by replacing tracking URLs with example.com.",
) {
    compatibleWith(
        "com.piccomaeurope.fr"(
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
        ),
    )

    execute {
        facebookSDKFingerprint.method.apply {
            instructions.filter { instruction ->
                instruction.opcode == Opcode.CONST_STRING
            }.forEach { instruction ->
                instruction as OneRegisterInstruction

                replaceInstruction(
                    instruction.location.index,
                    "const-string v${instruction.registerA}, \"example.com\"",
                )
            }
        }

        firebaseInstallFingerprint.method.apply {
            instructions.filter {
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
        }

        appMeasurementFingerprint.method.addInstruction(0, "return-void")
    }
}
