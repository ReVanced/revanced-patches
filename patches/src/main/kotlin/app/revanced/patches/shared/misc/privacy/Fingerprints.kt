package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.Opcode

internal val youTubeCopyTextFingerprintMethodMatch = firstMethodComposite {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        Opcode.IGET_OBJECT(),
        after(0..2, "text/plain"()),
        after(0..2, method("newPlainText")),
        after(0..2, Opcode.MOVE_RESULT_OBJECT()),
        after(0..2, method("setPrimaryClip"))
    )
}

internal val youTubeSystemShareSheetMethodMatch = firstMethodComposite {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        method("setClassName"),
        after(0..4, method("iterator")),
        after(0..15, allOf(Opcode.IGET_OBJECT(), type("Ljava/lang/String;"))),
        after(0..15, method("putExtra"))
    )
}

internal val youTubeShareSheetMethodMatch = firstMethodComposite {
    returnType("V")
    parameterTypes("L", "Ljava/util/Map;")
    instructions(
        Opcode.IGET_OBJECT(),
        after(allOf(Opcode.CHECK_CAST(), type("Ljava/lang/String;"))),
        after(Opcode.GOTO()),
        method("putExtra"),
        "YTShare_Logging_Share_Intent_Endpoint_Byte_Array"()
    )
}
