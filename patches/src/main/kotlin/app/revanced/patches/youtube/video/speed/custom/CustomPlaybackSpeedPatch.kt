package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.interaction.seekbar.customTapAndHoldFingerprint
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playservice.is_19_47_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.speed.settingsMenuVideoSpeedGroup
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableField

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/PlaybackSpeedMenuFilter;"

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/speed/CustomPlaybackSpeedPatch;"

internal val customPlaybackSpeedPatch = bytecodePatch(
    description = "Adds custom playback speed options.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        lithoFilterPatch,
        versionCheckPatch,
        recyclerViewTreeHookPatch,
        resourceMappingPatch
    )

    execute {
        addResources("youtube", "video.speed.custom.customPlaybackSpeedPatch")

        settingsMenuVideoSpeedGroup.addAll(
            listOf(
                SwitchPreference("revanced_custom_speed_menu"),
                SwitchPreference("revanced_restore_old_speed_menu"),
                TextPreference(
                    "revanced_custom_playback_speeds",
                    inputType = InputType.TEXT_MULTI_LINE
                )
            )
        )

        if (is_19_47_or_greater) {
            settingsMenuVideoSpeedGroup.add(
                TextPreference("revanced_speed_tap_and_hold", inputType = InputType.NUMBER_DECIMAL),
            )
        }

        // Override the min/max speeds that can be used.
        (if (is_20_34_or_greater) speedLimiterFingerprint else speedLimiterLegacyFingerprint).method.apply {
            val limitMinIndex = indexOfFirstLiteralInstructionOrThrow(0.25f)
            // Older unsupported targets use 2.0f and not 4.0f
            val limitMaxIndex = indexOfFirstLiteralInstructionOrThrow(4.0f)

            val limitMinRegister = getInstruction<OneRegisterInstruction>(limitMinIndex).registerA
            val limitMaxRegister = getInstruction<OneRegisterInstruction>(limitMaxIndex).registerA

            replaceInstruction(limitMinIndex, "const/high16 v$limitMinRegister, 0.0f")
            replaceInstruction(limitMaxIndex, "const/high16 v$limitMaxRegister, 8.0f")
        }

        // Turn off client side flag that use server provided min/max speeds.
        if (is_20_34_or_greater) {
            serverSideMaxSpeedFeatureFlagFingerprint.method.returnEarly(false)
        }

        // region Force old video quality menu.

        // Replace the speeds float array with custom speeds.
        speedArrayGeneratorFingerprint.let {
            val matches = it.instructionMatches
            it.method.apply {
                val playbackSpeedsArrayType = "$EXTENSION_CLASS_DESCRIPTOR->customPlaybackSpeeds:[F"
                // Apply changes from last index to first to preserve indexes.

                val originalArrayFetchIndex = matches[5].index
                val originalArrayFetchDestination = matches[5].getInstruction<OneRegisterInstruction>().registerA
                replaceInstruction(
                    originalArrayFetchIndex,
                    "sget-object v$originalArrayFetchDestination, $playbackSpeedsArrayType"
                )

                val arrayLengthConstDestination = matches[3].getInstruction<OneRegisterInstruction>().registerA
                val newArrayIndex = matches[4].index
                addInstructions(
                    newArrayIndex,
                    """
                        sget-object v$arrayLengthConstDestination, $playbackSpeedsArrayType
                        array-length v$arrayLengthConstDestination, v$arrayLengthConstDestination
                    """
                )

                val sizeCallIndex = matches[0].index + 1
                val sizeCallResultRegister = getInstruction<OneRegisterInstruction>(sizeCallIndex).registerA
                replaceInstruction(sizeCallIndex, "const/4 v$sizeCallResultRegister, 0x0")
            }
        }

        // Add a static INSTANCE field to the class.
        // This is later used to call "showOldPlaybackSpeedMenu" on the instance.

        val instanceField = ImmutableField(
            getOldPlaybackSpeedsFingerprint.originalClassDef.type,
            "INSTANCE",
            getOldPlaybackSpeedsFingerprint.originalClassDef.type,
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            null,
        ).toMutable()

        getOldPlaybackSpeedsFingerprint.classDef.staticFields.add(instanceField)
        // Set the INSTANCE field to the instance of the class.
        // In order to prevent a conflict with another patch, add the instruction at index 1.
        getOldPlaybackSpeedsFingerprint.method.addInstruction(1, "sput-object p0, $instanceField")

        // Get the "showOldPlaybackSpeedMenu" method.
        // This is later called on the field INSTANCE.
        val showOldPlaybackSpeedMenuMethod = showOldPlaybackSpeedMenuFingerprint.match(
            getOldPlaybackSpeedsFingerprint.classDef,
        ).method

        // Insert the call to the "showOldPlaybackSpeedMenu" method on the field INSTANCE.
        showOldPlaybackSpeedMenuExtensionFingerprint.method.apply {
            addInstructionsWithLabels(
                instructions.lastIndex,
                """
                    sget-object v0, $instanceField
                    if-nez v0, :not_null
                    return-void
                    :not_null
                    invoke-virtual { v0 }, $showOldPlaybackSpeedMenuMethod
                """
            )
        }

        // endregion

        // Close the unpatched playback dialog and show the custom speeds.
        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the playback speed menu is currently shown.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion

        // region Custom tap and hold 2x speed.

        if (is_19_47_or_greater) {
            customTapAndHoldFingerprint.let {
                it.method.apply {
                    val index = it.instructionMatches.first().index
                    val register = getInstruction<OneRegisterInstruction>(index).registerA

                    addInstructions(
                        index + 1,
                        """
                            invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->tapAndHoldSpeed()F
                            move-result v$register
                        """
                    )
                }
            }
        }

        // endregion
    }
}
