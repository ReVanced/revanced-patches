package app.revanced.patches.youtube.video.speed.custom.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.misc.bottomsheet.hook.patch.BottomSheetHookPatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.video.speed.custom.fingerprints.*
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.immutable.ImmutableField

@Name("custom-video-speed")
@Description("Adds custom video speed options.")
@DependsOn([IntegrationsPatch::class, LithoFilterPatch::class, SettingsPatch::class, BottomSheetHookPatch::class])
@Version("0.0.1")
class CustomVideoSpeedPatch : BytecodePatch(
    listOf(
        SpeedArrayGeneratorFingerprint,
        SpeedLimiterFingerprint,
        GetOldVideoSpeedsFingerprint,
        ShowOldVideoSpeedMenuIntegrationsFingerprint
    )
) {

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            TextPreference(
                key = "revanced_custom_playback_speeds",
                title = StringResource(
                    "revanced_custom_playback_speeds_title",
                    "Custom playback speeds"
                ),
                inputType = InputType.TEXT_MULTI_LINE,
                summary = StringResource(
                    "revanced_custom_playback_speeds_summary",
                    "Add or change the available playback speeds"
                )
            )
        )

        val arrayGenMethod = SpeedArrayGeneratorFingerprint.result?.mutableMethod!!
        val arrayGenMethodImpl = arrayGenMethod.implementation!!

        val sizeCallIndex = arrayGenMethodImpl.instructions
            .indexOfFirst { ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "size" }

        if (sizeCallIndex == -1) return PatchResultError("Couldn't find call to size()")

        val sizeCallResultRegister =
            (arrayGenMethodImpl.instructions.elementAt(sizeCallIndex + 1) as OneRegisterInstruction).registerA

        arrayGenMethod.replaceInstruction(
            sizeCallIndex + 1,
            "const/4 v$sizeCallResultRegister, 0x0"
        )

        val (arrayLengthConstIndex, arrayLengthConst) = arrayGenMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == 7 }

        val arrayLengthConstDestination = (arrayLengthConst as OneRegisterInstruction).registerA

        val videoSpeedsArrayType = "$INTEGRATIONS_CLASS_DESCRIPTOR->customVideoSpeeds:[F"

        arrayGenMethod.addInstructions(
            arrayLengthConstIndex + 1,
            """
                sget-object v$arrayLengthConstDestination, $videoSpeedsArrayType
                array-length v$arrayLengthConstDestination, v$arrayLengthConstDestination
            """
        )

        val (originalArrayFetchIndex, originalArrayFetch) = arrayGenMethodImpl.instructions.withIndex()
            .first {
                val reference = ((it.value as? ReferenceInstruction)?.reference as? FieldReference)
                reference?.definingClass?.contains("PlayerConfigModel") ?: false &&
                        reference?.type == "[F"
            }

        val originalArrayFetchDestination = (originalArrayFetch as OneRegisterInstruction).registerA

        arrayGenMethod.replaceInstruction(
            originalArrayFetchIndex,
            "sget-object v$originalArrayFetchDestination, $videoSpeedsArrayType"
        )

        val limiterMethod = SpeedLimiterFingerprint.result?.mutableMethod!!
        val limiterMethodImpl = limiterMethod.implementation!!

        val lowerLimitConst = 0.25f.toRawBits()
        val upperLimitConst = 2.0f.toRawBits()
        val (limiterMinConstIndex, limiterMinConst) = limiterMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == lowerLimitConst }
        val (limiterMaxConstIndex, limiterMaxConst) = limiterMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == upperLimitConst }

        val limiterMinConstDestination = (limiterMinConst as OneRegisterInstruction).registerA
        val limiterMaxConstDestination = (limiterMaxConst as OneRegisterInstruction).registerA

        // edit: alternatively this might work by overriding with fixed values such as 0.1x and 10x
        limiterMethod.replaceInstruction(
            limiterMinConstIndex,
            "sget v$limiterMinConstDestination, $INTEGRATIONS_CLASS_DESCRIPTOR->minVideoSpeed:F"
        )
        limiterMethod.replaceInstruction(
            limiterMaxConstIndex,
            "sget v$limiterMaxConstDestination, $INTEGRATIONS_CLASS_DESCRIPTOR->maxVideoSpeed:F"
        )

        // region Force old video quality menu.
        // This is necessary, because there is no known way of adding custom video speeds to the new menu.

        BottomSheetHookPatch.addHook(INTEGRATIONS_CLASS_DESCRIPTOR)

        // Required to check if the video speed menu is currently shown.
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        GetOldVideoSpeedsFingerprint.result?.let { result ->
            // Add a static INSTANCE field to the class.
            // This is later used to call "showOldVideoSpeedMenu" on the instance.
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

            // Get the "showOldVideoSpeedMenu" method.
            // This is later called on the field INSTANCE.
            val showOldVideoSpeedMenuMethod = ShowOldVideoSpeedMenuFingerprint.also {
                if (!it.resolve(context, result.classDef))
                    throw ShowOldVideoSpeedMenuFingerprint.toErrorResult()
            }.result!!.method.toString()

            // Insert the call to the "showOldVideoSpeedMenu" method on the field INSTANCE.
            ShowOldVideoSpeedMenuIntegrationsFingerprint.result?.mutableMethod?.apply {
                addInstructionsWithLabels(
                    implementation!!.instructions.lastIndex,
                    """
                        sget-object v0, $instanceField
                        if-nez v0, :not_null
                        return-void
                        :not_null
                        invoke-virtual { v0 }, $showOldVideoSpeedMenuMethod
                    """
                )
            } ?: return ShowOldVideoSpeedMenuIntegrationsFingerprint.toErrorResult()
        } ?: return GetOldVideoSpeedsFingerprint.toErrorResult()

        // endregion

        return PatchResultSuccess()
    }

    private companion object {
        private const val FILTER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/components/VideoSpeedMenuFilterPatch;"

        private const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/speed/CustomVideoSpeedPatch;"

    }
}
