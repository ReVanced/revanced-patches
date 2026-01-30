package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.youTubeCopyTextMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        Opcode.IGET_OBJECT(),
        afterAtMost(2, "text/plain"()),
        afterAtMost(2, method("newPlainText")),
        afterAtMost(2, Opcode.MOVE_RESULT_OBJECT()),
        afterAtMost(2, method("setPrimaryClip")),
    )
}

internal val BytecodePatchContext.youTubeSystemShareSheetMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        method("setClassName"),
        afterAtMost(4, method("iterator")),
        afterAtMost(15, allOf(Opcode.IGET_OBJECT(), field { type == "Ljava/lang/String;" })),
        afterAtMost(15, method("putExtra")),
    )
}

internal val BytecodePatchContext.youTubeShareSheetMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        Opcode.IGET_OBJECT(),
        after(allOf(Opcode.CHECK_CAST(), type("Ljava/lang/String;"))),
        after(Opcode.GOTO()),
        method("putExtra"),
        "YTShare_Logging_Share_Intent_Endpoint_Byte_Array"(),
    )
}
