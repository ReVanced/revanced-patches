package app.revanced.patches.reddit.customclients.redditisfun.api

import app.revanced.patcher.*
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val basicAuthorizationMethodMatch = firstMethodComposite {
    instructions(
        "yyOCBp.RHJhDKd"(),
        "fJOxVwBUyo*=f:<OoejWs:AqmIJ"() // Encrypted basic authorization string.
    )
}

internal val buildAuthorizationStringMethodMatch = firstMethodComposite {
    instructions(
        "yyOCBp.RHJhDKd"(),
        "client_id"()
    )
}

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
