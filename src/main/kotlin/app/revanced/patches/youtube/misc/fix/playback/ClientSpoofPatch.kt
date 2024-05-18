package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildInitPlaybackRequestFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildPlayerRequestURIFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.CreatePlayerRequestBodyFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.SetPlayerRequestClientTypeFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Client spoof",
    description = "Spoofs the client to allow video playback.",
    dependencies = [UserAgentClientSpoofPatch::class],
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube"),
    ],
)
object ClientSpoofPatch : BytecodePatch(
    setOf(
        SetPlayerRequestClientTypeFingerprint,
        CreatePlayerRequestBodyFingerprint,
        BuildInitPlaybackRequestFingerprint,
        BuildPlayerRequestURIFingerprint,
    ),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/ClientSpoofPatch;"
    private const val CLIENT_INFO_CLASS_DESCRIPTOR =
        "Lcom/google/protos/youtube/api/innertube/InnertubeContext\$ClientInfo;"

    override fun execute(context: BytecodeContext) {
        // region Block /initplayback requests to fall back to /get_watch requests.

        BuildInitPlaybackRequestFingerprint.result?.let {
            val moveUriStringIndex = it.scanResult.patternScanResult!!.startIndex
            val targetRegister = it.mutableMethod.getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

            it.mutableMethod.replaceInstruction(
                moveUriStringIndex,
                "const-string v$targetRegister, \"https://127.0.0.1\"",
            )
        } ?: throw BuildInitPlaybackRequestFingerprint.exception

        // endregion

        // region Block /get_watch requests to fall back to /player requests.

        BuildPlayerRequestURIFingerprint.result?.let {
            val invokeToStringIndex = it.scanResult.patternScanResult!!.startIndex
            val uriRegister = it.mutableMethod.getInstruction<FiveRegisterInstruction>(invokeToStringIndex).registerC

            it.mutableMethod.addInstructions(
                invokeToStringIndex,
                """
                   invoke-static { v$uriRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockGetWatchRequest(Landroid/net/Uri;)Landroid/net/Uri;
                   move-result-object v$uriRegister
                """,
            )
        } ?: throw BuildPlayerRequestURIFingerprint.exception

        // endregion

        // region Get field references to be used below.

        val (clientInfoField, clientInfoClientTypeField) = SetPlayerRequestClientTypeFingerprint.result?.let { result ->
            // Field in the player request object that holds the client info object.
            val clientInfoField = result.mutableMethod
                .getInstructions().first { instruction ->
                    // requestMessage.clientInfo = clientInfoBuilder.build();
                    instruction.opcode == Opcode.IPUT_OBJECT &&
                        instruction.getReference<FieldReference>()?.type == CLIENT_INFO_CLASS_DESCRIPTOR
                }.getReference<FieldReference>()

            // Client info object's client type field.
            val clientInfoClientTypeField = result.mutableMethod
                .getInstruction(result.scanResult.patternScanResult!!.endIndex)
                .getReference<FieldReference>()

            clientInfoField to clientInfoClientTypeField
        } ?: throw SetPlayerRequestClientTypeFingerprint.exception

        // endregion

        // region Spoof client type for /player requests to 5 (IOS).

        CreatePlayerRequestBodyFingerprint.result?.let { result ->
            val checkCastIndex = result.scanResult.patternScanResult!!.startIndex

            result.mutableMethod.apply {
                val requestMessageInstance = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

                val freeRegister1 = 5
                val freeRegister2 = 6

                val iosClientType = 5

                // Set requestMessage.clientInfo.clientType to ClientType.IOS.
                addInstructions(
                    checkCastIndex + 1,
                    """
                        iget-object v$freeRegister1, v$requestMessageInstance, $clientInfoField
                        const/4 v$freeRegister2, $iosClientType
                        iput v$freeRegister2, v$freeRegister1, $clientInfoClientTypeField
                    """,
                )
            }
        } ?: throw CreatePlayerRequestBodyFingerprint.exception

        // endregion
    }
}
