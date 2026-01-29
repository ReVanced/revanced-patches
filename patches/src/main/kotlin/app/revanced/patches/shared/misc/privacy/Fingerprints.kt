package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.youTubeCopyTextMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        Opcode.IGET_OBJECT(),
        after(0..2, "text/plain"()),
        after(0..2, method("newPlainText")),
        after(0..2, Opcode.MOVE_RESULT_OBJECT()),
        after(0..2, method("setPrimaryClip")),
    )
}

internal val BytecodePatchContext.youTubeSystemShareSheetMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        method("setClassName"),
        after(0..4, method("iterator")),
        after(0..15, allOf(Opcode.IGET_OBJECT(), type("Ljava/lang/String;"))),
        after(0..15, method("putExtra")),
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
