package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.speed.settingsMenuVideoSpeedGroup
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstruction
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/PlaybackSpeedMenuFilter;"

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/speed/CustomPlaybackSpeedPatch;"

internal var speedUnavailableId = -1L
    private set

private val customPlaybackSpeedResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        speedUnavailableId = resourceMappings["string", "varispeed_unavailable_message"]
    }
}

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
        customPlaybackSpeedResourcePatch
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
                ),
            )
        )

        if (is_19_25_or_greater) {
            settingsMenuVideoSpeedGroup.add(
                TextPreference("revanced_speed_tap_and_hold", inputType = InputType.NUMBER_DECIMAL),
            )
        }

        // Override the min/max speeds that can be used.
        speedLimiterFingerprint.method.apply {
            val limitMinIndex = indexOfFirstLiteralInstructionOrThrow(0.25f)
            var limitMaxIndex = indexOfFirstLiteralInstruction(2.0f)
            // Newer targets have 4x max speed.
            if (limitMaxIndex < 0) {
                limitMaxIndex = indexOfFirstLiteralInstructionOrThrow(4.0f)
            }

            val limitMinRegister = getInstruction<OneRegisterInstruction>(limitMinIndex).registerA
            val limitMaxRegister = getInstruction<OneRegisterInstruction>(limitMaxIndex).registerA

            replaceInstruction(limitMinIndex, "const/high16 v$limitMinRegister, 0.0f")
            replaceInstruction(limitMaxIndex, "const/high16 v$limitMaxRegister, 8.0f")
        }


        // Replace the speeds float array with custom speeds.
        // These speeds are used if the speed menu is immediately opened after a video is opened.
        speedArrayGeneratorFingerprint.method.apply {
            val sizeCallIndex = indexOfFirstInstructionOrThrow { getReference<MethodReference>()?.name == "size" }
            val sizeCallResultRegister = getInstruction<OneRegisterInstruction>(sizeCallIndex + 1).registerA

            replaceInstruction(sizeCallIndex + 1, "const/4 v$sizeCallResultRegister, 0x0")

            val arrayLengthConstIndex = indexOfFirstLiteralInstructionOrThrow(7)
            val arrayLengthConstDestination = getInstruction<OneRegisterInstruction>(arrayLengthConstIndex).registerA
            val playbackSpeedsArrayType = "$EXTENSION_CLASS_DESCRIPTOR->customPlaybackSpeeds:[F"

            addInstructions(
                arrayLengthConstIndex + 1,
                """
                    sget-object v$arrayLengthConstDestination, $playbackSpeedsArrayType
                    array-length v$arrayLengthConstDestination, v$arrayLengthConstDestination
                """,
            )

            val originalArrayFetchIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<FieldReference>()
                reference?.type == "[F" && reference.definingClass.endsWith("/PlayerConfigModel;")
            }
            val originalArrayFetchDestination =
                getInstruction<OneRegisterInstruction>(originalArrayFetchIndex).registerA

            replaceInstruction(
                originalArrayFetchIndex,
                "sget-object v$originalArrayFetchDestination, $playbackSpeedsArrayType",
            )
        }

        // region Force old video quality menu.

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

        // Close the unpatched playback dialog and show the modern custom dialog.
        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the playback speed menu is currently shown.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // region Custom tap and hold 2x speed.

        if (is_19_25_or_greater) {
            disableFastForwardNoticeFingerprint.method.apply {
                val index = indexOfFirstInstructionOrThrow {
                    (this as? NarrowLiteralInstruction)?.narrowLiteral == 2.0f.toRawBits()
                }
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

        // endregion
    }
}
