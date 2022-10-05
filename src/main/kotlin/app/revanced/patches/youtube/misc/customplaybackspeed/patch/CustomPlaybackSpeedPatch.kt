package app.revanced.patches.youtube.misc.customplaybackspeed.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.customplaybackspeed.annotations.CustomPlaybackSpeedCompatibility
import app.revanced.patches.youtube.misc.customplaybackspeed.fingerprints.SpeedArrayGeneratorFingerprint
import app.revanced.patches.youtube.misc.customplaybackspeed.fingerprints.SpeedLimiterFingerprint
import app.revanced.patches.youtube.misc.customplaybackspeed.fingerprints.VideoSpeedPatchFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import org.jf.dexlib2.builder.instruction.BuilderArrayPayload
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import java.util.stream.DoubleStream
import kotlin.math.roundToInt

@Patch
@Name("custom-playback-speed")
@Description("Adds more video playback speed options.")
@DependsOn([IntegrationsPatch::class, ResourceMappingResourcePatch::class])
@CustomPlaybackSpeedCompatibility
@Version("0.0.1")
class CustomPlaybackSpeedPatch : BytecodePatch(
    listOf(
        SpeedArrayGeneratorFingerprint, SpeedLimiterFingerprint, VideoSpeedPatchFingerprint
    )
) {

    override fun execute(context: BytecodeContext): PatchResult {
        //TODO: include setting to skip remembering the new speed

        val speedLimitMin = minVideoSpeed!!.toFloat()
        val speedLimitMax = maxVideoSpeed!!.toFloat().coerceAtLeast(speedLimitMin)
        val speedsGranularity = videoSpeedsGranularity!!.toFloat()

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

        val limiterMethod = SpeedLimiterFingerprint.result?.mutableMethod!!
        val limiterMethodImpl = limiterMethod.implementation!!

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

        val constructorResult = VideoSpeedPatchFingerprint.result!!
        val constructor = constructorResult.mutableMethod
        val implementation = constructor.implementation!!

        val stepsGranularity = 8F
        val step = speedLimitMax
            .minus(speedLimitMin) // calculate the range of the speeds
            .div(speedsGranularity)
            .times(stepsGranularity)
            .roundToInt()
            .div(stepsGranularity)// round to nearest multiple of stepsGranularity
            .coerceAtLeast(1 / stepsGranularity) // ensure steps are at least 1/8th of the step granularity

        val videoSpeedsArray = buildList<Number> {
            DoubleStream
                .iterate(speedLimitMin.toDouble()) { it + step } // create a stream of speeds
                .let { speedStream ->
                    for (speed in speedStream) {
                        if (speed > speedLimitMax) break
                        add(speed.toFloat().toRawBits())
                    }
                }
        }

        // adjust the new array of speeds size
        constructor.replaceInstruction(
            0,
            "const/16 v0, ${videoSpeedsArray.size}"
        )

        // create the payload with the new speeds
        val arrayPayloadIndex = implementation.instructions.size - 1
        implementation.replaceInstruction(
            arrayPayloadIndex,
            BuilderArrayPayload(
                4,
                videoSpeedsArray
            )
        )

        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        private fun String?.validate(max: Int? = null) = this?.toFloatOrNull() != null &&
                toFloat().let { float ->
                    float > 0 && max?.let { max -> float <= max } ?: true
                }

        val videoSpeedsGranularity by option(
            PatchOption.StringOption(
                "granularity",
                "16",
                "Video speed granularity",
                "The granularity of the video speeds. The higher the value, the more speeds will be available.",
                true
            ) {
                it.validate()
            }
        )

        val minVideoSpeed by option(
            PatchOption.StringOption(
                "min",
                "0.25",
                "Minimum video speed",
                "The minimum video speed.",
                true
            ) {
                it.validate()
            }
        )

        val maxVideoSpeed by option(
            PatchOption.StringOption(
                "max",
                "5.0",
                "Maximum video speed",
                "The maximum video speed. Must be greater than the minimum video speed and smaller than 5.",
                true
            ) {
                it.validate(5)
            }
        )
    }
}
