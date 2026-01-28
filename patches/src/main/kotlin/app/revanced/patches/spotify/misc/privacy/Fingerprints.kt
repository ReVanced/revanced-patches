package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.shareCopyUrlMethod by gettingFirstMutableMethodDeclarativelyOrNull(
    "clipboard",
    "Spotify Link",
) {
    name("invokeSuspend")
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
}

internal val BytecodePatchContext.oldShareCopyUrlMethod by gettingFirstMutableMethodDeclaratively(
    "clipboard",
    "createNewSession failed",
) {
    name("apply")
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
}

internal val BytecodePatchContext.formatAndroidShareSheetUrlMethod by gettingFirstMutableMethodDeclarativelyOrNull {
    returnType("Ljava/lang/String;")
    parameterTypes("L", "Ljava/lang/String;")
    opcodes(
        Opcode.GOTO,
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    literal { '\n'.code.toLong() }
}

internal val BytecodePatchContext.oldFormatAndroidShareSheetUrlMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/String;")
    parameterTypes("Lcom/spotify/share/social/sharedata/ShareData;", "Ljava/lang/String;")
    instructions('\n'.code.toLong()())
}
