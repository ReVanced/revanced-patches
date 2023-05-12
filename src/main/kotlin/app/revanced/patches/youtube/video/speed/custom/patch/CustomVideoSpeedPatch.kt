package app.revanced.patches.youtube.video.speed.custom.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.video.speed.custom.annotations.CustomPlaybackSpeedCompatibility
import app.revanced.patches.youtube.video.speed.custom.fingerprints.SpeedArrayGeneratorFingerprint
import app.revanced.patches.youtube.video.speed.custom.fingerprints.SpeedLimiterFingerprint
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference

@Name("custom-video-speed")
@Description("Adds custom video speed options.")
@DependsOn([IntegrationsPatch::class])
@CustomPlaybackSpeedCompatibility
@Version("0.0.1")
class CustomVideoSpeedPatch : BytecodePatch(
    listOf(
        SpeedArrayGeneratorFingerprint, SpeedLimiterFingerprint
    )
) {

    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = "revanced_custom_video_speeds",
                title = StringResource(
                    "revanced_custom_video_speeds_title",
                    "Custom playback speeds"
                ),
                inputType = InputType.TEXT_MULTI_LINE,
                summary = StringResource(
                    "revanced_custom_video_speeds_summary",
                    "Add or change the video speeds available"
                ),
                default = "0.25\n0.5\n0.75\n0.9\n0.95\n1.0\n1.05\n1.1\n1.25\n1.5\n1.75\n2.0\n3.0\n4.0\n5.0"
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

        val videoSpeedsArrayType = "Lapp/revanced/integrations/patches/playback/speed/CustomVideoSpeedPatch;->customVideoSpeeds:[F"

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

        val (limiterMinConstIndex, limiterMinConst) = limiterMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == 0.25f.toRawBits() }
        val (limiterMaxConstIndex, limiterMaxConst) = limiterMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == 2.0f.toRawBits() }

        val limiterMinConstDestination = (limiterMinConst as OneRegisterInstruction).registerA
        val limiterMaxConstDestination = (limiterMaxConst as OneRegisterInstruction).registerA

        // edit: alternatively this might work by overriding with fixed values such as 0.1x and 10x
        limiterMethod.replaceInstruction(
            limiterMinConstIndex,
            "sget v$limiterMinConstDestination, Lapp/revanced/integrations/patches/playback/speed/CustomVideoSpeedPatch;->minVideoSpeed:F"
        )
        limiterMethod.replaceInstruction(
            limiterMaxConstIndex,
            "sget v$limiterMaxConstDestination, Lapp/revanced/integrations/patches/playback/speed/CustomVideoSpeedPatch;->maxVideoSpeed:F"
        )

        return PatchResultSuccess()
    }

}
