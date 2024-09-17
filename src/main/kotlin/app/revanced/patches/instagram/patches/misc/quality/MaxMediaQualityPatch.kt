package app.revanced.patches.instagram.patches.misc.quality

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.patches.maxQuality.fingerprints.DisplayMetricsFingerprint
import app.revanced.patches.instagram.patches.maxQuality.fingerprints.MediaSizeFingerprint
import app.revanced.patches.instagram.patches.maxQuality.fingerprints.StoryMediaBitrateFingerprint
import app.revanced.patches.instagram.patches.maxQuality.fingerprints.VideoEncoderConfigFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Max media quality",
    description = "Sets the images/videos/stories quality to highest " +
        "to fight unwanted compression based on screen size.",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object MaxMediaQualityPatch : BytecodePatch(
    setOf(
        DisplayMetricsFingerprint,
        MediaSizeFingerprint,
        StoryMediaBitrateFingerprint,
        VideoEncoderConfigFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        val maxPostSize = "0x800" // Decimal value = 2048. Maximum post size.
        val maxBitRate = "0x989680" // Decimal value = 10000000. Maximum bit rate possible (found in code).

        // Improve quality of images.
        // Instagram tend to reduce/compress the image resolution to user's device height and width.
        // This section of code removes that restriction and sets the resolution to 2048x2048 (max possible).
        DisplayMetricsFingerprint.resultOrThrow().let { it ->
            it.mutableMethod.apply {
                val displayMetInstructions = getInstructions().filter { it.opcode == Opcode.IGET }

                // There are 3 iget instances.
                // 1.dpi 2.width 3.height.
                // We don't need to change dpi, we just need to change height and width.
                displayMetInstructions.drop(1).forEach { instruction ->
                    val index = instruction.location.index
                    val register = getInstruction<TwoRegisterInstruction>(index).registerA

                    // Set height and width to 2048.
                    addInstruction(index + 1, "const v$register, $maxPostSize")
                }
            }
        }

        // Yet another method where the image resolution is compressed.
        MediaSizeFingerprint.resultOrThrow().let { it ->
            it.mutableClass.apply {
                val mediaSetMethod =
                    methods.first { it.returnType == "Lcom/instagram/model/mediasize/ExtendedImageUrl;" }

                val mediaSetInstructions =
                    mediaSetMethod.getInstructions().filter { it.opcode == Opcode.INVOKE_VIRTUAL }

                mediaSetInstructions.forEach { instruction ->
                    val index = instruction.location.index + 1
                    val register = mediaSetMethod.getInstruction<OneRegisterInstruction>(index).registerA

                    // Set height and width to 2048.
                    mediaSetMethod.addInstruction(index + 1, "const v$register, $maxPostSize")
                }
            }
        }

        // Improve quality of stories.
        // This section of code sets the bitrate of the stories to the max possible.
        StoryMediaBitrateFingerprint.resultOrThrow().let { it ->
            it.mutableMethod.apply {
                val ifLezIndex = getInstructions().first { it.opcode == Opcode.IF_LEZ }.location.index

                val bitRateRegister = getInstruction<OneRegisterInstruction>(ifLezIndex).registerA

                // Set the bitrate to maximum possible.
                addInstruction(ifLezIndex + 1, "const v$bitRateRegister, $maxBitRate")
            }
        }

        // Improve quality of reels.
        // In general Instagram tend to set the minimum bitrate between max possible and compressed video's bitrate
        // This section of code sets the bitrate of the reels to the max possible.
        VideoEncoderConfigFingerprint.resultOrThrow().let { it ->
            it.mutableClass.apply {
                // Get the constructor.
                val videoEncoderConfigConstructor = methods.first()

                val lastMoveResIndex = videoEncoderConfigConstructor.getInstructions()
                    .last { it.opcode == Opcode.MOVE_RESULT }.location.index

                // Finding the register were the bitrate is stored.
                val bitRateRegister =
                    videoEncoderConfigConstructor.getInstruction<OneRegisterInstruction>(lastMoveResIndex).registerA

                // Set bitrate to maximum possible.
                videoEncoderConfigConstructor.addInstruction(
                    lastMoveResIndex + 1,
                    "const v$bitRateRegister, $maxBitRate",
                )
            }
        }
    }
}
