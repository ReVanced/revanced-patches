package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.*
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
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
    name = "Spoof client",
    description = "Spoofs the client to allow video playback.",
    dependencies = [
        SettingsPatch::class,
        AddResourcesPatch::class,
        UserAgentClientSpoofPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
            ],
        ),
    ],
)
object SpoofClientPatch : BytecodePatch(
    setOf(
        // Client type spoof.
        BuildInitPlaybackRequestFingerprint,
        BuildPlayerRequestURIFingerprint,
        SetPlayerRequestClientTypeFingerprint,
        CreatePlayerRequestBodyFingerprint,
        CreatePlayerRequestBodyWithModelFingerprint,

        // Player gesture config.
        PlayerGestureConfigSyntheticFingerprint,

        // Player speed menu item.
        CreatePlaybackSpeedMenuItemFingerprint,
    ),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/SpoofClientPatch;"
    private const val CLIENT_INFO_CLASS_DESCRIPTOR =
        "Lcom/google/protos/youtube/api/innertube/InnertubeContext\$ClientInfo;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            PreferenceScreen(
                key = "revanced_spoof_client_screen",
                sorting = PreferenceScreen.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_client"),
                    SwitchPreference("revanced_spoof_client_use_ios"),
                ),
            ),
        )

        // region Block /initplayback requests to fall back to /get_watch requests.

        BuildInitPlaybackRequestFingerprint.resultOrThrow().let {
            val moveUriStringIndex = it.scanResult.patternScanResult!!.startIndex

            it.mutableMethod.apply {
                val targetRegister = getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

                addInstructions(
                    moveUriStringIndex + 1,
                    """
                        invoke-static { v$targetRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockInitPlaybackRequest(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$targetRegister
                    """,
                )
            }
        }

        // endregion

        // region Block /get_watch requests to fall back to /player requests.

        BuildPlayerRequestURIFingerprint.resultOrThrow().let {
            val invokeToStringIndex = it.scanResult.patternScanResult!!.startIndex

            it.mutableMethod.apply {
                val uriRegister = getInstruction<FiveRegisterInstruction>(invokeToStringIndex).registerC

                addInstructions(
                    invokeToStringIndex,
                    """
                        invoke-static { v$uriRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockGetWatchRequest(Landroid/net/Uri;)Landroid/net/Uri;
                        move-result-object v$uriRegister
                    """,
                )
            }
        }

        // endregion

        // region Get field references to be used below.

        val (clientInfoField, clientInfoClientTypeField, clientInfoClientVersionField) =
            SetPlayerRequestClientTypeFingerprint.resultOrThrow().let { result ->
                // Field in the player request object that holds the client info object.
                val clientInfoField = result.mutableMethod
                    .getInstructions().find { instruction ->
                        // requestMessage.clientInfo = clientInfoBuilder.build();
                        instruction.opcode == Opcode.IPUT_OBJECT &&
                            instruction.getReference<FieldReference>()?.type == CLIENT_INFO_CLASS_DESCRIPTOR
                    }?.getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoField")

                // Client info object's client type field.
                val clientInfoClientTypeField = result.mutableMethod
                    .getInstruction(result.scanResult.patternScanResult!!.endIndex)
                    .getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoClientTypeField")

                // Client info object's client version field.
                val clientInfoClientVersionField = result.mutableMethod
                    .getInstruction(result.scanResult.stringsScanResult!!.matches.first().index + 1)
                    .getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoClientVersionField")

                Triple(clientInfoField, clientInfoClientTypeField, clientInfoClientVersionField)
            }

        val clientInfoClientModelField = CreatePlayerRequestBodyWithModelFingerprint.resultOrThrow().let {
            val getClientModelIndex = CreatePlayerRequestBodyWithModelFingerprint.indexOfBuildModelInstruction(it.method)

            // The next IPUT_OBJECT instruction after getting the client model is setting the client model field.
            val index = it.mutableMethod.indexOfFirstInstructionOrThrow(getClientModelIndex) {
                opcode == Opcode.IPUT_OBJECT
            }

            it.mutableMethod.getInstruction(index).getReference<FieldReference>()
                ?: throw PatchException("Could not find clientInfoClientModelField")
        }

        // endregion

        // region Spoof client type for /player requests.

        CreatePlayerRequestBodyFingerprint.resultOrThrow().let { result ->
            val setClientInfoMethodName = "patch_setClientInfo"
            val checkCastIndex = result.scanResult.patternScanResult!!.startIndex
            var clientInfoContainerClassName: String

            result.mutableMethod.apply {
                val checkCastInstruction = getInstruction<OneRegisterInstruction>(checkCastIndex)
                val requestMessageInstanceRegister = checkCastInstruction.registerA
                clientInfoContainerClassName = checkCastInstruction.getReference<TypeReference>()!!.type

                addInstruction(
                    checkCastIndex + 1,
                    "invoke-static { v$requestMessageInstanceRegister }," +
                        " ${result.classDef.type}->$setClientInfoMethodName($clientInfoContainerClassName)V",
                )
            }

            // Change client info to use the spoofed values.
            // Do this in a helper method, to remove the need of picking out multiple free registers from the hooked code.
            result.mutableClass.methods.add(
                ImmutableMethod(
                    result.mutableClass.type,
                    setClientInfoMethodName,
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
                            
                            # Set client type to the spoofed value.
                            iget v1, v0, $clientInfoClientTypeField
                            invoke-static { v1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->getClientTypeId(I)I
                            move-result v1
                            iput v1, v0, $clientInfoClientTypeField
                            
                            # Set client model to the spoofed value.
                            iget-object v1, v0, $clientInfoClientModelField
                            invoke-static { v1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->getClientModel(Ljava/lang/String;)Ljava/lang/String;
                            move-result-object v1
                            iput-object v1, v0, $clientInfoClientModelField

                            # Set client version to the spoofed value.
                            iget-object v1, v0, $clientInfoClientVersionField
                            invoke-static { v1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->getClientVersion(Ljava/lang/String;)Ljava/lang/String;
                            move-result-object v1
                            iput-object v1, v0, $clientInfoClientVersionField
                            
                            :disabled
                            return-void
                        """,
                    )
                },
            )
        }

        // endregion

        // region Fix player gesture if spoofing to iOS.

        PlayerGestureConfigSyntheticFingerprint.resultOrThrow().let {
            val endIndex = it.scanResult.patternScanResult!!.endIndex
            val downAndOutLandscapeAllowedIndex = endIndex - 3
            val downAndOutPortraitAllowedIndex = endIndex - 9

            arrayOf(
                downAndOutLandscapeAllowedIndex,
                downAndOutPortraitAllowedIndex,
            ).forEach { index ->
                val gestureAllowedMethod = context.toMethodWalker(it.mutableMethod)
                    .nextMethod(index, true)
                    .getMethod() as MutableMethod

                gestureAllowedMethod.apply {
                    val isAllowedIndex = getInstructions().lastIndex
                    val isAllowed = getInstruction<OneRegisterInstruction>(isAllowedIndex).registerA

                    addInstructions(
                        isAllowedIndex,
                        """
                            invoke-static { v$isAllowed }, $INTEGRATIONS_CLASS_DESCRIPTOR->enablePlayerGesture(Z)Z
                            move-result v$isAllowed
                        """,
                    )
                }
            }
        }

        // endregion

        // Fix playback speed menu item if spoofing to iOS.

        CreatePlaybackSpeedMenuItemFingerprint.resultOrThrow().let {
            val scanResult = it.scanResult.patternScanResult!!
            if (scanResult.startIndex != 0) throw PatchException("Unexpected start index: ${scanResult.startIndex}")

            it.mutableMethod.apply {
                // Find the conditional check if the playback speed menu item is not created.
                val shouldCreateMenuIndex = indexOfFirstInstructionOrThrow(scanResult.endIndex) { opcode == Opcode.IF_EQZ }
                val shouldCreateMenuRegister = getInstruction<OneRegisterInstruction>(shouldCreateMenuIndex).registerA

                addInstructions(
                    shouldCreateMenuIndex,
                    """
                        invoke-static { v$shouldCreateMenuRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->forceCreatePlaybackSpeedMenu(Z)Z
                        move-result v$shouldCreateMenuRegister
                    """,
                )
            }
        }

        // endregion
    }
}
