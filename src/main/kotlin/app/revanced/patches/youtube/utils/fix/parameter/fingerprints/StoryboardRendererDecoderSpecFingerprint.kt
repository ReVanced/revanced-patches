package app.revanced.patches.youtube.utils.fix.parameter.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
* Resolves to the same method as [StoryboardRendererDecoderRecommendedLevelFingerprint].
*/
internal object StoryboardRendererDecoderSpecFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"),
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE, // First instruction of the method.
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
    ),
    strings = listOf("#-1#")
)
