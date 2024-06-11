package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/spoof/SpoofClientPatch;"
private const val CLIENT_INFO_CLASS_DESCRIPTOR =
    "Lcom/google/protos/youtube/api/innertube/InnertubeContext\$ClientInfo;"

val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to allow video playback.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
        userAgentClientSpoofPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
        ),
    )

    // Client type spoof.
    val buildInitPlaybackRequestResult by buildInitPlaybackRequestFingerprint
    val buildPlayerRequestURIResult by buildPlayerRequestURIFingerprint
    val setPlayerRequestClientTypeResult by setPlayerRequestClientTypeFingerprint
    val createPlayerRequestBodyResult by createPlayerRequestBodyFingerprint
    val createPlayerRequestBodyWithModelResult by createPlayerRequestBodyWithModelFingerprint
    // Player gesture config.
    val playerGestureConfigSyntheticResult by playerGestureConfigSyntheticFingerprint
    // Player speed menu item.
    val createPlaybackSpeedMenuItemResult by createPlaybackSpeedMenuItemFingerprint

    execute { context ->
        addResources("youtube", "misc.fix.playback.spoofClientPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_spoof_client_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_client"),
                    SwitchPreference("revanced_spoof_client_use_ios"),
                ),
            ),
        )

        // region Block /initplayback requests to fall back to /get_watch requests.

        val moveUriStringIndex = buildInitPlaybackRequestResult.scanResult.patternScanResult!!.startIndex

        buildInitPlaybackRequestResult.mutableMethod.apply {
            val targetRegister = getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

            addInstructions(
                moveUriStringIndex + 1,
                """
                    invoke-static { v$targetRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockInitPlaybackRequest(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$targetRegister
                """,
            )
        }

        // endregion

        // region Block /get_watch requests to fall back to /player requests.

        val invokeToStringIndex = buildPlayerRequestURIResult.scanResult.patternScanResult!!.startIndex

        buildPlayerRequestURIResult.mutableMethod.apply {
            val uriRegister = getInstruction<FiveRegisterInstruction>(invokeToStringIndex).registerC

            addInstructions(
                invokeToStringIndex,
                """
                    invoke-static { v$uriRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockGetWatchRequest(Landroid/net/Uri;)Landroid/net/Uri;
                    move-result-object v$uriRegister
                """,
            )
        }

        // endregion

        // region Get field references to be used below.

        // Field in the player request object that holds the client info object.
        val clientInfoField = setPlayerRequestClientTypeResult.mutableMethod
            .instructions.find { instruction ->
                // requestMessage.clientInfo = clientInfoBuilder.build();
                instruction.opcode == Opcode.IPUT_OBJECT &&
                    instruction.getReference<FieldReference>()?.type == CLIENT_INFO_CLASS_DESCRIPTOR
            }?.getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoField")

        // Client info object's client type field.
        val clientInfoClientTypeField = setPlayerRequestClientTypeResult.mutableMethod
            .getInstruction(setPlayerRequestClientTypeResult.scanResult.patternScanResult!!.endIndex)
            .getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoClientTypeField")

        // Client info object's client version field.
        val clientInfoClientVersionField = setPlayerRequestClientTypeResult.mutableMethod
            .getInstruction(setPlayerRequestClientTypeResult.scanResult.stringsScanResult!!.matches.first().index + 1)
            .getReference<FieldReference>()
            ?: throw PatchException("Could not find clientInfoClientVersionField")

        val getClientModelIndex = indexOfBuildModelInstruction(createPlayerRequestBodyWithModelResult.method)

        // The next IPUT_OBJECT instruction after getting the client model is setting the client model field.
        val index = createPlayerRequestBodyWithModelResult.mutableMethod
            .indexOfFirstInstructionOrThrow(getClientModelIndex) {
                opcode == Opcode.IPUT_OBJECT
            }

        val clientInfoClientModelField = createPlayerRequestBodyWithModelResult.mutableMethod
            .getInstruction(index)
            .getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoClientModelField")

        // endregion

        // region Spoof client type for /player requests.

        val setClientInfoMethodName = "patch_setClientInfo"
        val checkCastIndex = createPlayerRequestBodyResult.scanResult.patternScanResult!!.startIndex
        var clientInfoContainerClassName: String

        createPlayerRequestBodyResult.mutableMethod.apply {
            val checkCastInstruction = getInstruction<OneRegisterInstruction>(checkCastIndex)
            val requestMessageInstanceRegister = checkCastInstruction.registerA
            clientInfoContainerClassName = checkCastInstruction.getReference<TypeReference>()!!.type

            addInstruction(
                checkCastIndex + 1,
                "invoke-static { v$requestMessageInstanceRegister }," +
                    " ${createPlayerRequestBodyResult.classDef.type}->" +
                    "$setClientInfoMethodName($clientInfoContainerClassName)V",
            )
        }

        // Change client info to use the spoofed values.
        // Do this in a helper method, to remove the need of picking out multiple free registers from the hooked code.
        createPlayerRequestBodyResult.mutableClass.methods.add(
            ImmutableMethod(
                createPlayerRequestBodyResult.mutableClass.type,
                setClientInfoMethodName,
                listOf(ImmutableMethodParameter(clientInfoContainerClassName, null, "clientInfoContainer")),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
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

        // endregion

        // region Fix player gesture if spoofing to iOS.

        val endIndex = playerGestureConfigSyntheticResult.scanResult.patternScanResult!!.endIndex
        val downAndOutLandscapeAllowedIndex = endIndex - 3
        val downAndOutPortraitAllowedIndex = endIndex - 9

        arrayOf(
            downAndOutLandscapeAllowedIndex,
            downAndOutPortraitAllowedIndex,
        ).forEach { index ->
            val gestureAllowedMethod = context.navigate(playerGestureConfigSyntheticResult.mutableMethod)
                .at(index)
                .mutable()

            gestureAllowedMethod.apply {
                val isAllowedIndex = instructions.lastIndex
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

        // endregion

        // Fix playback speed menu item if spoofing to iOS.

        val scanResult = createPlaybackSpeedMenuItemResult.scanResult.patternScanResult!!
        if (scanResult.startIndex != 0) throw PatchException("Unexpected start index: ${scanResult.startIndex}")

        createPlaybackSpeedMenuItemResult.mutableMethod.apply {
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

        // endregion
    }
}