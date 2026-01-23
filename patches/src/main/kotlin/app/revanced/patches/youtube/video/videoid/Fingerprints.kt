package app.revanced.patches.youtube.video.videoid

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.videoIdMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        methodCall(
            definingClass = "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;",
            returnType = "Ljava/lang/String;",
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT),
    )
}

internal val BytecodePatchContext.videoIdBackgroundPlayMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.DECLARED_SYNCHRONIZED, AccessFlags.FINAL, AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes("L")
    instructions(
        methodCall(
            definingClass = "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;",
            returnType = "Ljava/lang/String;",
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT),
        opcode(Opcode.IPUT_OBJECT),
        opcode(Opcode.MONITOR_EXIT),
        opcode(Opcode.RETURN_VOID),
        opcode(Opcode.MONITOR_EXIT),
        opcode(Opcode.RETURN_VOID),
    )
    custom { method, classDef ->
        method.implementation != null &&
            (
                classDef.methods.count() == 17 || // 20.39 and lower.
                    classDef.methods.count() == 16
                ) // 20.40+
    }
}

internal val videoIdParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("[L")
    parameterTypes("L")
    instructions(
        524288L(),
    )
}
