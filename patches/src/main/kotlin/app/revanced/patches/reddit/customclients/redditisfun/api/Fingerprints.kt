package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.basicAuthorizationMethod by gettingFirstMutableMethodDeclaratively(
    "yyOCBp.RHJhDKd",
    "fJOxVwBUyo*=f:<OoejWs:AqmIJ" // Encrypted basic authorization string.
)

internal val BytecodePatchContext.buildAuthorizationStringMethod by gettingFirstMutableMethodDeclaratively(
    "yyOCBp.RHJhDKd",
    "client_id"
)

internal val BytecodePatchContext.getUserAgentMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        Opcode.NEW_ARRAY(),
        Opcode.CONST_4(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.APUT_OBJECT(),
        Opcode.CONST(),
    )
}
