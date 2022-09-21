package app.revanced.patches.youtube.layout.hidetimeandseekbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hidetimeandseekbar.annotations.HideTimeAndSeekbarCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("inline-time-bar-wrapper-fingerprint")
@MatchingMethod("Lcom/google/android/apps/youtube/app/common/player/overlay/InlineTimeBarWrapper;", "onLayout")
@FuzzyPatternScanMethod(3)
@HideTimeAndSeekbarCompatibility
@Version("0.0.1")
object InlineTimeBarWrapperFingerprint : MethodFingerprint(
    "V", AccessFlags.PROTECTED or AccessFlags.FINAL, listOf("Z", "I", "I", "I", "I"), listOf(
        Opcode.SUB_INT_2ADDR,
        Opcode.SUB_INT,
        Opcode.IF_EQZ,
        Opcode.IF_NEZ,
        Opcode.GOTO_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.SUB_INT,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IGET_OBJECT,
    )
)