package app.revanced.patches.reddit.customclients.joeyforreddit.api

import app.revanced.patcher.*
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.authUtilityUserAgentMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    opcodes(Opcode.APUT_OBJECT)
    custom { immutableClassDef.sourceFile == "AuthUtility.java" }
}

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    custom { immutableClassDef.sourceFile == "AuthUtility.java" }
}
