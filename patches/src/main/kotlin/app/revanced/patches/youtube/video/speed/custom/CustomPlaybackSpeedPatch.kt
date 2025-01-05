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
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField

var speedUnavailableId = -1L
    internal set

private val customPlaybackSpeedResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        speedUnavailableId = resourceMappings[
            "string",
            "varispeed_unavailable_message",
        ]
    }
}

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/PlaybackSpeedMenuFilterPatch;"

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/speed/CustomPlaybackSpeedPatch;"

internal val customPlaybackSpeedPatch = bytecodePatch(
    description = "Adds custom playback speed options.",
) {
    dependsOn(
        sharedExtensionPatch,
        lithoFilterPatch,
        settingsPatch,
        recyclerViewTreeHookPatch,
        customPlaybackSpeedResourcePatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "video.speed.custom.customPlaybackSpeedPatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_custom_speed_menu"),
            TextPreference("revanced_custom_playback_speeds", inputType = InputType.TEXT_MULTI_LINE),
        )

        // Replace the speeds float array with custom speeds.
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

        // Override the min/max speeds that can be used.
        speedLimiterFingerprint.method.apply {
            val limitMinIndex = indexOfFirstLiteralInstructionOrThrow(0.25f.toRawBits().toLong())
            var limitMaxIndex = indexOfFirstLiteralInstruction(2.0f.toRawBits().toLong())
            // Newer targets have 4x max speed.
            if (limitMaxIndex < 0) {
                limitMaxIndex = indexOfFirstLiteralInstructionOrThrow(4.0f.toRawBits().toLong())
            }

            val limitMinRegister = getInstruction<OneRegisterInstruction>(limitMinIndex).registerA
            val limitMaxRegister = getInstruction<OneRegisterInstruction>(limitMaxIndex).registerA

            replaceInstruction(limitMinIndex, "const/high16 v$limitMinRegister, 0.0f")
            replaceInstruction(limitMaxIndex, "const/high16 v$limitMaxRegister, 8.0f")
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
        ).method.toString()

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
                """,
            )
        }

        // region Force old video quality menu.
        // This is necessary, because there is no known way of adding custom playback speeds to the new menu.

        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the playback speed menu is currently shown.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }
}
