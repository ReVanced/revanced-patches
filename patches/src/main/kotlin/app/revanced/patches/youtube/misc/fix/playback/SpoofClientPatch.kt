package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.backgroundplayback.backgroundPlaybackPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
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

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/spoof/SpoofClientPatch;"
private const val CLIENT_INFO_CLASS_DESCRIPTOR =
    "Lcom/google/protos/youtube/api/innertube/InnertubeContext\$ClientInfo;"
private const val REQUEST_CLASS_DESCRIPTOR =
    "Lorg/chromium/net/ExperimentalUrlRequest;"
private const val REQUEST_BUILDER_CLASS_DESCRIPTOR =
    "Lorg/chromium/net/ExperimentalUrlRequest\$Builder;"

val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to allow video playback.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
        userAgentClientSpoofPatch,

        // Required since iOS livestream fix partially enables background playback.
        backgroundPlaybackPatch,
        playerTypeHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            // This patch works with these versions,
            // but the dependent background playback patch does not.
            // "18.37.36",
            // "18.38.44",
            // "18.43.45",
            // "18.44.41",
            // "18.45.43",
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
    val buildInitPlaybackRequestMatch by buildInitPlaybackRequestFingerprint()
    val buildPlayerRequestURIMatch by buildPlayerRequestURIFingerprint()
    val setPlayerRequestClientTypeMatch by setPlayerRequestClientTypeFingerprint()
    val createPlayerRequestBodyMatch by createPlayerRequestBodyFingerprint()
    val createPlayerRequestBodyWithModelMatch by createPlayerRequestBodyWithModelFingerprint()
    val createPlayerRequestBodyWithVersionReleaseMatch by createPlayerRequestBodyWithVersionReleaseFingerprint()
    // Player gesture config.
    val playerGestureConfigSyntheticMatch by playerGestureConfigSyntheticFingerprint()
    // Player speed menu item.
    val createPlaybackSpeedMenuItemMatch by createPlaybackSpeedMenuItemFingerprint()
    // Video qualities missing.
    val buildRequestMatch by buildRequestFingerprint()
    // Livestream audio only background playback.
    val playerResponseModelBackgroundAudioPlaybackMatch by playerResponseModelBackgroundAudioPlaybackFingerprint()

    execute { context ->
        addResources("youtube", "misc.fix.playback.spoofClientPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_spoof_client_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_client"),
                    ListPreference(
                        "revanced_spoof_client_type",
                        summaryKey = null,
                        entriesKey = "revanced_spoof_client_type_entries",
                        entryValuesKey = "revanced_spoof_client_type_entry_values",
                    ),
                    SwitchPreference("revanced_spoof_client_ios_force_avc"),
                    NonInteractivePreference("revanced_spoof_client_about_android_ios"),
                    NonInteractivePreference("revanced_spoof_client_about_android_vr"),
                ),
            ),
        )

        // region Block /initplayback requests to fall back to /get_watch requests.

        val moveUriStringIndex = buildInitPlaybackRequestMatch.patternMatch!!.startIndex

        buildInitPlaybackRequestMatch.mutableMethod.apply {
            val targetRegister = getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

            addInstructions(
                moveUriStringIndex + 1,
                """
                    invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->blockInitPlaybackRequest(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$targetRegister
                """,
            )
        }

        // endregion

        // region Block /get_watch requests to fall back to /player requests.

        val invokeToStringIndex = buildPlayerRequestURIMatch.patternMatch!!.startIndex

        buildPlayerRequestURIMatch.mutableMethod.apply {
            val uriRegister = getInstruction<FiveRegisterInstruction>(invokeToStringIndex).registerC

            addInstructions(
                invokeToStringIndex,
                """
                    invoke-static { v$uriRegister }, $EXTENSION_CLASS_DESCRIPTOR->blockGetWatchRequest(Landroid/net/Uri;)Landroid/net/Uri;
                    move-result-object v$uriRegister
                """,
            )
        }

        // endregion

        // region Get field references to be used below.

        // Field in the player request object that holds the client info object.
        val clientInfoField = setPlayerRequestClientTypeMatch.mutableMethod
            .instructions.find { instruction ->
                // requestMessage.clientInfo = clientInfoBuilder.build();
                instruction.opcode == Opcode.IPUT_OBJECT &&
                    instruction.getReference<FieldReference>()?.type == CLIENT_INFO_CLASS_DESCRIPTOR
            }?.getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoField")

        // Client info object's client type field.
        val clientInfoClientTypeField = setPlayerRequestClientTypeMatch.mutableMethod
            .getInstruction(setPlayerRequestClientTypeMatch.patternMatch!!.endIndex)
            .getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoClientTypeField")

        // Client info object's client version field.
        val clientInfoClientVersionField = setPlayerRequestClientTypeMatch.mutableMethod
            .getInstruction(setPlayerRequestClientTypeMatch.stringMatches!!.first().index + 1)
            .getReference<FieldReference>()
            ?: throw PatchException("Could not find clientInfoClientVersionField")

        val clientInfoClientModelField = createPlayerRequestBodyWithModelMatch.let {
            val getClientModelIndex = indexOfBuildModelInstruction(it.method)

            // The next IPUT_OBJECT instruction after getting the client model is setting the client model field.
            val index = it.mutableMethod.indexOfFirstInstructionOrThrow(getClientModelIndex) {
                opcode == Opcode.IPUT_OBJECT
            }

            it.mutableMethod
                .getInstruction(index)
                .getReference<FieldReference>() ?: throw PatchException("Could not find clientInfoClientModelField")
        }

        val clientInfoOsVersionField = createPlayerRequestBodyWithVersionReleaseMatch.let {
            val getOsVersionIndex = indexOfBuildVersionReleaseInstruction(it.method)

            // The next IPUT_OBJECT instruction after getting the client OS version
            // is setting the client OS version field.
            val index = it.mutableMethod.indexOfFirstInstructionOrThrow(getOsVersionIndex) {
                opcode == Opcode.IPUT_OBJECT
            }

            it.mutableMethod.getInstruction(index).getReference<FieldReference>()
                ?: throw PatchException("Could not find clientInfoOsVersionField")
        }

        // endregion

        // region Spoof client type for /player requests.

        val setClientInfoMethodName = "patch_setClientInfo"
        val checkCastIndex = createPlayerRequestBodyMatch.patternMatch!!.startIndex
        var clientInfoContainerClassName: String

        createPlayerRequestBodyMatch.mutableMethod.apply {
            val checkCastInstruction = getInstruction<OneRegisterInstruction>(checkCastIndex)
            val requestMessageInstanceRegister = checkCastInstruction.registerA
            clientInfoContainerClassName = checkCastInstruction.getReference<TypeReference>()!!.type

            addInstruction(
                checkCastIndex + 1,
                "invoke-static { v$requestMessageInstanceRegister }," +
                    " ${createPlayerRequestBodyMatch.classDef.type}->" +
                    "$setClientInfoMethodName($clientInfoContainerClassName)V",
            )
        }

        // Change client info to use the spoofed values.
        // Do this in a helper method, to remove the need of picking out multiple free registers from the hooked code.
        createPlayerRequestBodyMatch.mutableClass.methods.add(
            ImmutableMethod(
                createPlayerRequestBodyMatch.mutableClass.type,
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
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->isClientSpoofingEnabled()Z
                        move-result v0
                        if-eqz v0, :disabled
                        
                        iget-object v0, p0, $clientInfoField
                        
                        # Set client type to the spoofed value.
                        iget v1, v0, $clientInfoClientTypeField
                        invoke-static { v1 }, $EXTENSION_CLASS_DESCRIPTOR->getClientTypeId(I)I
                        move-result v1
                        iput v1, v0, $clientInfoClientTypeField
                        
                        # Set client model to the spoofed value.
                        iget-object v1, v0, $clientInfoClientModelField
                        invoke-static { v1 }, $EXTENSION_CLASS_DESCRIPTOR->getClientModel(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v1
                        iput-object v1, v0, $clientInfoClientModelField

                        # Set client version to the spoofed value.
                        iget-object v1, v0, $clientInfoClientVersionField
                        invoke-static { v1 }, $EXTENSION_CLASS_DESCRIPTOR->getClientVersion(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v1
                        iput-object v1, v0, $clientInfoClientVersionField
                        
                        # Set client os version to the spoofed value.
                        iget-object v1, v0, $clientInfoOsVersionField
                        invoke-static { v1 }, $EXTENSION_CLASS_DESCRIPTOR->getOsVersion(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v1
                        iput-object v1, v0, $clientInfoOsVersionField
                       
                        :disabled
                        return-void
                    """,
                )
            },
        )

        // endregion

        // region Fix player gesture if spoofing to iOS.

        val endIndex = playerGestureConfigSyntheticMatch.patternMatch!!.endIndex
        val downAndOutLandscapeAllowedIndex = endIndex - 3
        val downAndOutPortraitAllowedIndex = endIndex - 9

        arrayOf(
            downAndOutLandscapeAllowedIndex,
            downAndOutPortraitAllowedIndex,
        ).forEach { index ->
            val gestureAllowedMethod = context.navigate(playerGestureConfigSyntheticMatch.mutableMethod)
                .at(index)
                .mutable()

            gestureAllowedMethod.apply {
                val isAllowedIndex = instructions.lastIndex
                val isAllowed = getInstruction<OneRegisterInstruction>(isAllowedIndex).registerA

                addInstructions(
                    isAllowedIndex,
                    """
                        invoke-static { v$isAllowed }, $EXTENSION_CLASS_DESCRIPTOR->enablePlayerGesture(Z)Z
                        move-result v$isAllowed
                    """,
                )
            }
        }

        // endregion

        // Fix playback speed menu item if spoofing to iOS.

        val patternMatch = createPlaybackSpeedMenuItemMatch.patternMatch!!
        if (patternMatch.startIndex != 0) throw PatchException("Unexpected start index: ${patternMatch.startIndex}")

        createPlaybackSpeedMenuItemMatch.mutableMethod.apply {
            // Find the conditional check if the playback speed menu item is not created.
            val shouldCreateMenuIndex =
                indexOfFirstInstructionOrThrow(patternMatch.endIndex) { opcode == Opcode.IF_EQZ }
            val shouldCreateMenuRegister = getInstruction<OneRegisterInstruction>(shouldCreateMenuIndex).registerA

            addInstructions(
                shouldCreateMenuIndex,
                """
                    invoke-static { v$shouldCreateMenuRegister }, $EXTENSION_CLASS_DESCRIPTOR->forceCreatePlaybackSpeedMenu(Z)Z
                    move-result v$shouldCreateMenuRegister
                """,
            )
        }

        // endregion
        // region Fix livestream audio only background play if spoofing to iOS.
        // This force enables audio background playback.

        playerResponseModelBackgroundAudioPlaybackMatch.mutableMethod.addInstructions(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->overrideBackgroundAudioPlayback()Z
                move-result v0
                if-eqz v0, :do_not_override
                return v0
                :do_not_override
                nop
            """,
        )

        // endregion

        // region Fix video qualities missing, if spoofing to iOS by overriding the user agent.

        buildRequestMatch.mutableMethod.apply {
            val buildRequestIndex = instructions.lastIndex - 2
            val requestBuilderRegister = getInstruction<FiveRegisterInstruction>(buildRequestIndex).registerC

            val newRequestBuilderIndex = buildRequestMatch.patternMatch!!.endIndex
            val urlRegister = getInstruction<FiveRegisterInstruction>(newRequestBuilderIndex).registerD

            // Replace "requestBuilder.build(): Request" with "overrideUserAgent(requestBuilder, url): Request".
            replaceInstruction(
                buildRequestIndex,
                "invoke-static { v$requestBuilderRegister, v$urlRegister }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->" +
                    "overrideUserAgent(${REQUEST_BUILDER_CLASS_DESCRIPTOR}Ljava/lang/String;)" +
                    REQUEST_CLASS_DESCRIPTOR,
            )
        }
        // endregion
    }
}
