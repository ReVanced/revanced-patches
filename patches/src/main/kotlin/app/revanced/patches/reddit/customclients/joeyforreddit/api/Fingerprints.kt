package app.revanced.patches.reddit.customclients.infinity.api

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.classDef
import app.revanced.patcher.custom
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.authUtilityUserAgentMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/String;")
    instructions(Opcode.APUT_OBJECT())
    custom { classDef.sourceFile == "AuthUtility.java" }
}

internal val BytecodePatchContext.getClientIdMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    instructions(
        Opcode.CONST(),
        Opcode.INVOKE_STATIC(),
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.RETURN_OBJECT(),
    )
    custom { classDef.sourceFile == "AuthUtility.java" }
}
