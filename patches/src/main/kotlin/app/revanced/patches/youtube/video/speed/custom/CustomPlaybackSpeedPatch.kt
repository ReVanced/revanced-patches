package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.addRecyclerViewTreeHook
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.recyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.matchOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
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

private const val EXTENSION_CLASS_DESCRIPTOR =
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

    val speedArrayGeneratorMatch by speedArrayGeneratorFingerprint()
    val speedLimiterMatch by speedLimiterFingerprint()
    val getOldPlaybackSpeedsMatch by getOldPlaybackSpeedsFingerprint()
    val showOldPlaybackSpeedMenuExtensionMatch by showOldPlaybackSpeedMenuExtensionFingerprint()

    execute { context ->
        addResources("youtube", "video.speed.custom.customPlaybackSpeedPatch")

        PreferenceScreen.VIDEO.addPreferences(
            TextPreference("revanced_custom_playback_speeds", inputType = InputType.TEXT_MULTI_LINE),
        )

        speedArrayGeneratorMatch.mutableMethod.apply {
            val sizeCallIndex = instructions
                .indexOfFirst { it.getReference<MethodReference>()?.name == "size" }
            if (sizeCallIndex == -1) throw PatchException("Couldn't find call to size()")

            val sizeCallResultRegister = getInstruction<OneRegisterInstruction>(sizeCallIndex + 1).registerA
            replaceInstruction(sizeCallIndex + 1, "const/4 v$sizeCallResultRegister, 0x0")

            val arrayLengthConstInstruction = instructions
                .first { (it as? NarrowLiteralInstruction)?.narrowLiteral == 7 }
            val arrayLengthConstRegister = (arrayLengthConstInstruction as OneRegisterInstruction).registerA
            val playbackSpeedsArrayType = "$EXTENSION_CLASS_DESCRIPTOR->customPlaybackSpeeds:[F"

            addInstructions(
                arrayLengthConstInstruction.location.index + 1,
                """
                    sget-object v$arrayLengthConstRegister, $playbackSpeedsArrayType
                    array-length v$arrayLengthConstRegister, v$arrayLengthConstRegister
                """,
            )

            val getOriginalArrayInstruction = instructions.first {
                val reference = it.getReference<FieldReference>() ?: return@first false

                reference.definingClass.contains("PlayerConfigModel") && reference.type == "[F"
            }

            val originalArrayFetchDestination = (getOriginalArrayInstruction as OneRegisterInstruction).registerA

            replaceInstruction(
                getOriginalArrayInstruction.location.index,
                "sget-object v$originalArrayFetchDestination, $playbackSpeedsArrayType",
            )
        }

        speedLimiterMatch.mutableMethod.apply {
            val lowerLimitConst = 0.25f.toRawBits()
            val upperLimitConst = 2.0f.toRawBits()
            val limiterMinConstInstruction = instructions
                .first { (it as? NarrowLiteralInstruction)?.narrowLiteral == lowerLimitConst }
            val limiterMaxConstInstruction = instructions
                .first { (it as? NarrowLiteralInstruction)?.narrowLiteral == upperLimitConst }

            val limiterMinConstDestination = (limiterMinConstInstruction as OneRegisterInstruction).registerA
            val limiterMaxConstDestination = (limiterMaxConstInstruction as OneRegisterInstruction).registerA

            replaceInstruction(
                limiterMinConstInstruction.location.index,
                "const/high16 v$limiterMinConstDestination, 0x0",
            )
            replaceInstruction(
                limiterMaxConstInstruction.location.index,
                "const/high16 v$limiterMaxConstDestination, 0x41200000  # 10.0f",
            )
        }

        // region Force old video quality menu.
        // This is necessary, because there is no known way of adding custom playback speeds to the new menu.

        addRecyclerViewTreeHook(EXTENSION_CLASS_DESCRIPTOR)

        // Required to check if the playback speed menu is currently shown.
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // Add a static INSTANCE field to the class.
        // This is later used to call "showOldPlaybackSpeedMenu" on the instance.
        val instanceField = ImmutableField(
            getOldPlaybackSpeedsMatch.classDef.type,
            "INSTANCE",
            getOldPlaybackSpeedsMatch.classDef.type,
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            null,
        ).toMutable().also {
            getOldPlaybackSpeedsMatch.mutableClass.staticFields.add(it)
        }

        // Set the INSTANCE field to the instance of the class.
        // In order to prevent a conflict with another patch, add the instruction at index 1.
        getOldPlaybackSpeedsMatch.mutableMethod.addInstruction(1, "sput-object p0, $instanceField")

        // Get the "showOldPlaybackSpeedMenu" method.
        // This is later called on the field INSTANCE.
        val showOldPlaybackSpeedMenuMethod = showOldPlaybackSpeedMenuFingerprint.apply {
            match(context, getOldPlaybackSpeedsMatch.classDef)
        }.matchOrThrow().method

        // Insert the call to the "showOldPlaybackSpeedMenu" method on the field INSTANCE.
        showOldPlaybackSpeedMenuExtensionMatch.mutableMethod.apply {
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

        // endregion
    }
}
