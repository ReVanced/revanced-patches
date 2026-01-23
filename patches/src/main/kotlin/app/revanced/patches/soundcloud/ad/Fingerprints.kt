package app.revanced.patches.soundcloud.ad

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val interceptMethodMatch = firstMethodComposite("SC-Mob-UserPlan", "Configuration") {
    accessFlags(AccessFlags.PUBLIC)
    returnType("L")
    parameterTypes("L")
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    )
}

internal val BytecodePatchContext.userConsumerPlanConstructorMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
        "Ljava/util/List;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    )
}
