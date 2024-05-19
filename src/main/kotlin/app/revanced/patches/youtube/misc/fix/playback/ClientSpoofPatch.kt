package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildInitPlaybackRequestFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildPlayerRequestURIFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.CreatePlayerRequestBodyFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.SetPlayerRequestClientTypeFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

@Patch(
    name = "Client spoof",
    description = "Spoofs the client to allow video playback.",
    dependencies = [SettingsPatch::class, UserAgentClientSpoofPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
            ]
        )
    ],
)
object ClientSpoofPatch : BytecodePatch(
    setOf(
        BuildInitPlaybackRequestFingerprint,
        BuildPlayerRequestURIFingerprint,
        SetPlayerRequestClientTypeFingerprint,
        CreatePlayerRequestBodyFingerprint,
    ),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/ClientSpoofPatch;"
    private const val CLIENT_INFO_CLASS_DESCRIPTOR =
        "Lcom/google/protos/youtube/api/innertube/InnertubeContext\$ClientInfo;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_spoof_client"),
        )

        // region Block /initplayback requests to fall back to /get_watch requests.

        BuildInitPlaybackRequestFingerprint.result?.let {
            val moveUriStringIndex = it.scanResult.patternScanResult!!.startIndex
            val targetRegister = it.mutableMethod.getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

            it.mutableMethod.addInstructions(
                moveUriStringIndex + 1,
                    """
                        invoke-static { v$targetRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockInitPlaybackRequest(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$targetRegister
                    """
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
            val setClientInfoToiOSMethodName = "patch_setClientInfoToiOS"
            val checkCastIndex = result.scanResult.patternScanResult!!.startIndex
            var clientInfoContainerClassName : String

            result.mutableMethod.apply {
                val checkCastInstruction = getInstruction<OneRegisterInstruction>(checkCastIndex)
                val requestMessageInstanceRegister = checkCastInstruction.registerA
                clientInfoContainerClassName = checkCastInstruction.getReference<TypeReference>()!!.type

                addInstruction(
                    checkCastIndex + 1,
                    "invoke-static { v$requestMessageInstanceRegister }," +
                            " ${result.classDef.type}->$setClientInfoToiOSMethodName($clientInfoContainerClassName)V"
                )
            }

            // Set requestMessage.clientInfo.clientType to ClientType.IOS.
            // Do this in a helper method, to remove the need of picking out multiple free registers from the hooked code.
            val iosClientType = 5
            result.mutableClass.methods.add(
                ImmutableMethod(
                    result.mutableClass.type,
                    setClientInfoToiOSMethodName,
                    listOf(ImmutableMethodParameter(clientInfoContainerClassName, null, "clientInfoContainer")),
                    "V",
                    AccessFlags.PRIVATE or AccessFlags.STATIC,
                    null,
                    null,
                    MutableMethodImplementation(3),
                ).toMutable().apply {
                    addInstructions(
                        """
                            invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->isClientSpoofingEnabled()Z
                            move-result v0
                            if-eqz v0, :disabled
                            iget-object v0, p0, $clientInfoField
                            const/4 v1, $iosClientType
                            iput v1, v0, $clientInfoClientTypeField
                            :disabled                           
                            return-void
                        """
                    )
                }
            )

        } ?: throw CreatePlayerRequestBodyFingerprint.exception

        // endregion
    }
}
