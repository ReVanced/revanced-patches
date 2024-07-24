package app.revanced.patches.soundcloud.offlinesync

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.soundcloud.offlinesync.fingerprints.DownloadOperationsHeaderVerificationFingerprint
import app.revanced.patches.soundcloud.offlinesync.fingerprints.DownloadOperationsURLBuilderFingerprint
import app.revanced.patches.soundcloud.shared.fingerprints.FeatureConstructorFingerprint
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Enable offline sync",
    compatiblePackages = [CompatiblePackage("com.soundcloud.android")],
)
@Suppress("unused")
object EnableOfflineSyncPatch : BytecodePatch(
    setOf(
        FeatureConstructorFingerprint, DownloadOperationsURLBuilderFingerprint,
        DownloadOperationsHeaderVerificationFingerprint
    ),
) {
    override fun execute(context: BytecodeContext) {
        // Enable the feature to allow offline track syncing by modifying the JSON server response.
        // This method is the constructor of a class representing a "Feature" object parsed from JSON data.
        // p1 is the name of the feature.
        // p2 is true if the feature is enabled, false otherwise.
        FeatureConstructorFingerprint.resultOrThrow().mutableMethod.apply {
            val afterCheckNotNullIndex = 2

            addInstructionsWithLabels(
                afterCheckNotNullIndex,
                """
                    const-string v0, "offline_sync"
                    invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :skip
                    const/4 p2, 0x1
                """,
                ExternalLabel("skip", getInstruction(afterCheckNotNullIndex)),
            )
        }

        // Patch the URL builder to use the HTTPS_STREAM endpoint
        // instead of the offline sync endpoint to downloading the track.
        DownloadOperationsURLBuilderFingerprint.resultOrThrow().mutableMethod.apply {
            val getEndpointsEnumFieldIndex = 1
            val getEndpointsEnumFieldInstruction = getInstruction<OneRegisterInstruction>(getEndpointsEnumFieldIndex)

            val targetRegister = getEndpointsEnumFieldInstruction.registerA
            val endpointsType = getEndpointsEnumFieldInstruction.getReference<FieldReference>()!!.type

            replaceInstruction(
                getEndpointsEnumFieldIndex,
                "sget-object v$targetRegister, $endpointsType->HTTPS_STREAM:$endpointsType"
            )
        }

        // The HTTPS_STREAM endpoint does not return the necessary headers for offline sync.
        // Mock the headers to prevent the app from crashing by setting them to empty strings.
        // The headers are all cosmetic and do not affect the functionality of the app.
        DownloadOperationsHeaderVerificationFingerprint.resultOrThrow().mutableMethod.apply {
            // The first three null checks need to be patched.
            getInstructions().asSequence().filter {
                it.opcode == Opcode.IF_EQZ
            }.take(3).toList().map { it.location.index }.asReversed().forEach { nullCheckIndex ->
                val headerStringRegister = getInstruction<OneRegisterInstruction>(nullCheckIndex).registerA

                addInstruction(nullCheckIndex, "const-string v$headerStringRegister, \"\"")
            }
        }
    }
}
