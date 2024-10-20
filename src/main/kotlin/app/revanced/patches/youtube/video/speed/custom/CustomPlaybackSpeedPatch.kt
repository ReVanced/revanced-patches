package app.revanced.patches.youtube.video.speed.custom

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.RecyclerViewTreeHookPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.speed.custom.fingerprints.*
import app.revanced.util.alsoResolve
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField

@Patch(
    description = "Adds custom playback speed options.",
    dependencies = [
        IntegrationsPatch::class,
        LithoFilterPatch::class,
        SettingsPatch::class,
        RecyclerViewTreeHookPatch::class,
        CustomPlaybackSpeedResourcePatch::class,
        AddResourcesPatch::class
    ]
)
object CustomPlaybackSpeedPatch : BytecodePatch(
    setOf(
        SpeedArrayGeneratorFingerprint,
        SpeedLimiterFingerprint,
        GetOldPlaybackSpeedsFingerprint,
        ShowOldPlaybackSpeedMenuIntegrationsFingerprint
    )
) {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/PlaybackSpeedMenuFilterPatch;"

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/playback/speed/CustomPlaybackSpeedPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            TextPreference("revanced_custom_playback_speeds", inputType = InputType.TEXT_MULTI_LINE)
        )

        // Replace the speeds float array with custom speeds.
        SpeedArrayGeneratorFingerprint.resultOrThrow().mutableMethod.apply {
            val sizeCallIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.name == "size"
            }
            val sizeCallResultRegister = getInstruction<OneRegisterInstruction>(
                sizeCallIndex + 1
            ).registerA

            replaceInstruction(
                sizeCallIndex + 1,
                "const/4 v$sizeCallResultRegister, 0x0"
            )

            val arrayLengthConstIndex = indexOfFirstWideLiteralInstructionValueOrThrow(7)
            val arrayLengthConstDestination = getInstruction<OneRegisterInstruction>(
                arrayLengthConstIndex
            ).registerA
            val playbackSpeedsArrayType = "$INTEGRATIONS_CLASS_DESCRIPTOR->customPlaybackSpeeds:[F"

            addInstructions(
                arrayLengthConstIndex + 1,
                """
                    sget-object v$arrayLengthConstDestination, $playbackSpeedsArrayType
                    array-length v$arrayLengthConstDestination, v$arrayLengthConstDestination
                """
            )

            val originalArrayFetchIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<FieldReference>()
                reference?.type == "[F" && reference.definingClass.endsWith("/PlayerConfigModel;")
            }
            val originalArrayFetchDestination = getInstruction<OneRegisterInstruction>(
                originalArrayFetchIndex
            ).registerA

            replaceInstruction(
                originalArrayFetchIndex,
                "sget-object v$originalArrayFetchDestination, $playbackSpeedsArrayType"
            )
        }

        // Override the min/max speeds that can be used.
        SpeedLimiterFingerprint.resultOrThrow().mutableMethod.apply {
            val limiterMinConstIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                0.25f.toRawBits().toLong()
            )
            var limiterMaxConstIndex = indexOfFirstWideLiteralInstructionValue(
                2.0f.toRawBits().toLong()
            )
            // Newer targets have 4x max speed.
            if (limiterMaxConstIndex < 0) {
                limiterMaxConstIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                    4.0f.toRawBits().toLong()
                )
            }

            val limiterMinConstDestination = getInstruction<OneRegisterInstruction>(limiterMinConstIndex).registerA
            val limiterMaxConstDestination = getInstruction<OneRegisterInstruction>(limiterMaxConstIndex).registerA

            replaceInstruction(
                limiterMinConstIndex,
                "const/high16 v$limiterMinConstDestination, 0.0f"
            )
            replaceInstruction(
                limiterMaxConstIndex,
                "const/high16 v$limiterMaxConstDestination, 10.0f"
            )
        }

        GetOldPlaybackSpeedsFingerprint.resultOrThrow().let { result ->
            // Add a static INSTANCE field to the class.
            // This is later used to call "showOldPlaybackSpeedMenu" on the instance.
            val instanceField = ImmutableField(
                result.classDef.type,
                "INSTANCE",
                result.classDef.type,
                AccessFlags.PUBLIC or AccessFlags.STATIC,
                null,
                null,
                null
            ).toMutable()

            result.mutableClass.staticFields.add(instanceField)
            // Set the INSTANCE field to the instance of the class.
            // In order to prevent a conflict with another patch, add the instruction at index 1.
            result.mutableMethod.addInstruction(1, "sput-object p0, $instanceField")

            // Get the "showOldPlaybackSpeedMenu" method.
            // This is later called on the field INSTANCE.
            val showOldPlaybackSpeedMenuMethod = ShowOldPlaybackSpeedMenuFingerprint.alsoResolve(
                context,
                GetOldPlaybackSpeedsFingerprint
            ).method.toString()

            // Insert the call to the "showOldPlaybackSpeedMenu" method on the field INSTANCE.
            ShowOldPlaybackSpeedMenuIntegrationsFingerprint.resultOrThrow().mutableMethod.apply {
                addInstructionsWithLabels(
                    implementation!!.instructions.lastIndex,
                    """
                        sget-object v0, $instanceField
                        if-nez v0, :not_null
                        return-void
                        :not_null
                        invoke-virtual { v0 }, $showOldPlaybackSpeedMenuMethod
                    """
                )
            }
        }

        // region Force old video quality menu.
        // This is necessary, because there is no known way of adding custom playback speeds to the new menu.

        RecyclerViewTreeHookPatch.addHook(INTEGRATIONS_CLASS_DESCRIPTOR)

        // Required to check if the playback speed menu is currently shown.
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        // endregion
    }
}
