package app.revanced.patches.piccomafr.tracking

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.piccomafr.tracking.fingerprints.FacebookSDKFingerprint
import app.revanced.patches.piccomafr.tracking.fingerprints.FirebaseInstallFingerprint
import app.revanced.patches.piccomafr.tracking.fingerprints.AppMesurementFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import java.util.logging.Logger



@Patch(
    name = "Disable tracking",
    description = "Enable every debug possibilities of the app.",
    compatiblePackages = [CompatiblePackage(
        "com.piccomaeurope.fr",
        [
            "6.4.0", "6.4.1", "6.4.2", "6.4.3", "6.4.4", "6.4.5",
            "6.5.0", "6.5.1", "6.5.2", "6.5.3", "6.5.4",
            "6.6.0", "6.6.1", "6.6.2"
        ],
    )]
)
@Suppress("unused")
object DisableTrackingPatch : BytecodePatch(
    setOf(FacebookSDKFingerprint, FirebaseInstallFingerprint, AppMesurementFingerprint),
) {
    private val logger = Logger.getLogger(DisableTrackingPatch::class.java.name)

    override fun execute(context: BytecodeContext) {
        val fbSDK = FacebookSDKFingerprint.result?.mutableMethod
            ?: throw FacebookSDKFingerprint.exception

        fbSDK.getInstructions().withIndex()
            .filter { (_, instruction) -> instruction.opcode == Opcode.CONST_STRING }
            .forEach { (index, instruction) ->
                instruction as Instruction21c
                fbSDK.replaceInstruction(
                    index,
                    BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instruction.registerA,
                        ImmutableStringReference("example.com"),
                    ),
                )
            }

        // =====

        val firebaseInit = FirebaseInstallFingerprint.result?.mutableMethod
            ?: throw  FirebaseInstallFingerprint.exception

        firebaseInit.getInstructions().withIndex()
            .filter { (_, instruction) -> instruction.opcode == Opcode.CONST_STRING }
            .filter { (_, instruction) -> ((instruction as? Instruction21c)?.reference as? StringReference)?.string == "firebaseinstallations.googleapis.com" }
            .forEach { (index, instruction) ->
                instruction as Instruction21c
                firebaseInit.replaceInstruction(
                    index,
                    BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instruction.registerA,
                        ImmutableStringReference("example.com"),
                    ),
                )
            }

        // =====

        AppMesurementFingerprint.result?.mutableMethod?.addInstruction(
            0, "return-void"
        ) ?: throw  AppMesurementFingerprint.exception
    }
}
