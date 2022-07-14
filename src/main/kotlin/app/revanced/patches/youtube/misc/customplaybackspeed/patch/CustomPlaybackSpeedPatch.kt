package app.revanced.patches.youtube.misc.customplaybackspeed.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.customplaybackspeed.annotations.CustomPlaybackSpeedCompatibility
import app.revanced.patches.youtube.misc.customplaybackspeed.fingerprints.SpeedArrayGeneratorFingerprint
import app.revanced.patches.youtube.misc.customplaybackspeed.fingerprints.SpeedLimiterFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@Name("custom-playback-speed")
@Description("Allows to change the default playback speed options.")
@Dependencies(dependencies = [IntegrationsPatch::class])
@CustomPlaybackSpeedCompatibility
@Version("0.0.1")
class CustomPlaybackSpeedPatch : BytecodePatch(
    listOf(
        SpeedArrayGeneratorFingerprint, SpeedLimiterFingerprint
    )
) {

    override fun execute(data: BytecodeData): PatchResult {
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

        val videoSpeedsArrayType = "Lapp/revanced/integrations/patches/VideoSpeedPatch;->videoSpeeds:[F"

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

        val limiterMethod = SpeedLimiterFingerprint.result?.mutableMethod!!;
        val limiterMethodImpl = limiterMethod.implementation!!

        val speedLimitMin = 0.25f
        val speedLimitMax = 100f

        val (limiterMinConstIndex, limiterMinConst) = limiterMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == 0.25f.toRawBits() }
        val (limiterMaxConstIndex, limiterMaxConst) = limiterMethodImpl.instructions.withIndex()
            .first { (it.value as? NarrowLiteralInstruction)?.narrowLiteral == 2.0f.toRawBits() }

        val limiterMinConstDestination = (limiterMinConst as OneRegisterInstruction).registerA
        val limiterMaxConstDestination = (limiterMaxConst as OneRegisterInstruction).registerA

        fun hexFloat(float: Float): String = "0x%08x".format(float.toRawBits())

        limiterMethod.replaceInstruction(
            limiterMinConstIndex,
            "const/high16 v$limiterMinConstDestination, ${hexFloat(speedLimitMin)}"
        )
        limiterMethod.replaceInstruction(
            limiterMaxConstIndex,
            "const/high16 v$limiterMaxConstDestination, ${hexFloat(speedLimitMax)}"
        )

        return PatchResultSuccess()
    }
}
