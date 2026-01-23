package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.shareCopyUrlMethod by gettingFirstMethodDeclaratively {
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
    strings("clipboard", "Spotify Link")
    custom { method, _ ->
        method.name == "invokeSuspend"
    }
}

internal val BytecodePatchContext.oldShareCopyUrlMethod by gettingFirstMethodDeclaratively {
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
    strings("clipboard", "createNewSession failed")
    custom { method, _ ->
        method.name == "apply"
    }
}

internal val BytecodePatchContext.formatAndroidShareSheetUrlMethod by gettingFirstMethodDeclaratively {
    returnType("Ljava/lang/String;")
    parameterTypes("L", "Ljava/lang/String;")
    opcodes(
        Opcode.GOTO,
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    literal {
        '\n'.code.toLong()
    }
}

internal val BytecodePatchContext.oldFormatAndroidShareSheetUrlMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/String;")
    parameterTypes("Lcom/spotify/share/social/sharedata/ShareData;", "Ljava/lang/String;")
    literal {
        '\n'.code.toLong()
    }
}
