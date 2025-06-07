package app.revanced.patches.youtube.video.videoid

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val videoIdFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        methodCall(
            definingClass = "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;",
            returnType = "Ljava/lang/String;"
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT),
    )
}

internal val videoIdBackgroundPlayFingerprint by fingerprint {
    accessFlags(AccessFlags.DECLARED_SYNCHRONIZED, AccessFlags.FINAL, AccessFlags.PUBLIC)
    returns("V")
    parameters("L")
    instructions(
        methodCall(
            definingClass = "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;",
            returnType = "Ljava/lang/String;"
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT),
        opcode(Opcode.IPUT_OBJECT),
        opcode(Opcode.MONITOR_EXIT),
        opcode(Opcode.RETURN_VOID),
        opcode(Opcode.MONITOR_EXIT),
        opcode(Opcode.RETURN_VOID)
    )
    // The target snippet of code is buried in a huge switch block and the target method
    // has been changed many times by YT which makes identifying it more difficult than usual.
    custom { method, classDef ->
        classDef.methods.count() == 17 &&
                method.implementation != null
    }
}

internal val videoIdParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("[L")
    parameters("L")
    instructions(
        literal(524288L)
    )
}
