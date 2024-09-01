package app.revanced.patches.instagram.misc.maxQuality

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.misc.maxQuality.fingerprints.DisplayMetricsFingerprint
import app.revanced.patches.instagram.misc.maxQuality.fingerprints.MediaSizeFingerprint
import app.revanced.patches.instagram.misc.maxQuality.fingerprints.StoryMediaBitrateFingerprint
import app.revanced.patches.instagram.misc.maxQuality.fingerprints.VideoEncoderConfigFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Max media quality",
    description = "Makes media quality to max",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false
)
@Suppress("unused")
object MaxMediaQualityPatch:BytecodePatch(
    setOf(DisplayMetricsFingerprint,MediaSizeFingerprint,StoryMediaBitrateFingerprint,VideoEncoderConfigFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        val MAX_POST_SIZE = "0x800" //dec = 2048
        val MAX_BITRATE = "0x989680"//dec = 10000000

        ////display metrics
        val displayMetResult = DisplayMetricsFingerprint.result ?: throw DisplayMetricsFingerprint.exception
        val displayMetMethod = displayMetResult.mutableMethod
        val displayMetInstructions = displayMetMethod.getInstructions().filter { it.opcode == Opcode.IGET }

        //there are 3 iget instances
        // 1.dpi 2.width 3.height
        //we dont need to dpi, we need to change only height and width
        displayMetInstructions.drop(1).forEach { instruction ->
            val loc = instruction.location.index
            val reg = displayMetMethod.getInstruction<TwoRegisterInstruction>(loc).registerA

            //set height and width to 2048
            displayMetMethod.addInstruction(loc+1,"const v$reg, $MAX_POST_SIZE")
        }

        ////media size
        val mediaSizeResult = MediaSizeFingerprint.result ?: throw MediaSizeFingerprint.exception
        val mediaSizeDisplayClass = mediaSizeResult.mutableClass
        val mediaSetMethod = mediaSizeDisplayClass.methods.first { it.returnType == "Lcom/instagram/model/mediasize/ExtendedImageUrl;" }

        val mediaSetInstructions = mediaSetMethod.getInstructions().filter { it.opcode == Opcode.INVOKE_VIRTUAL }

        mediaSetInstructions.forEach { instruction ->
            val loc = instruction.location.index +1
            val reg = mediaSetMethod.getInstruction<OneRegisterInstruction>(loc).registerA

            //set height and width to 2048
            mediaSetMethod.addInstruction(loc+1,"const v$reg, $MAX_POST_SIZE")
        }

        ////stories
        val bitRateResult = StoryMediaBitrateFingerprint.result ?: throw StoryMediaBitrateFingerprint.exception
        val bitRateMethod = bitRateResult.mutableMethod
        val ifLezLoc = bitRateMethod.getInstructions().first { it.opcode == Opcode.IF_LEZ }.location.index

        val bitRateReg = bitRateMethod.getInstruction<OneRegisterInstruction>(ifLezLoc).registerA

        //set bitrate to 10000000
        bitRateMethod.addInstruction(ifLezLoc+1,"const v$bitRateReg, $MAX_BITRATE")

        ////video bitrate
        val videoEncoderResult = VideoEncoderConfigFingerprint.result ?: throw VideoEncoderConfigFingerprint.exception
        //get constructor
        val videoEncoderConfigConst = videoEncoderResult.mutableClass.methods.first()

        val lastMoveRes = videoEncoderConfigConst.getInstructions().last { it.opcode == Opcode.MOVE_RESULT }.location.index

        //bitrate reg
        val bitRate2Reg = videoEncoderConfigConst.getInstruction<OneRegisterInstruction>(lastMoveRes).registerA

        //set bitrate to 10000000
        videoEncoderConfigConst.addInstruction(lastMoveRes+1,"const v$bitRate2Reg, $MAX_BITRATE")


    }
}