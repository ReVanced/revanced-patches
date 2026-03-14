package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.shareCopyUrlMethod by gettingFirstMethodDeclarativelyOrNull(
    "clipboard",
    "Spotify Link",
) {
    name("invokeSuspend")
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
}

internal val BytecodePatchContext.oldShareCopyUrlMethod by gettingFirstMethodDeclaratively(
    "clipboard",
    "createNewSession failed",
) {
    name("apply")
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
}

internal val BytecodePatchContext.formatAndroidShareSheetUrlMethod by gettingFirstMethodDeclarativelyOrNull {
    returnType("Ljava/lang/String;")
    parameterTypes("L", "Ljava/lang/String;")
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    )
    instructions('\n'.code.toLong()())
}

internal val BytecodePatchContext.oldFormatAndroidShareSheetUrlMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Ljava/lang/String;")
    parameterTypes("Lcom/spotify/share/social/sharedata/ShareData;", "Ljava/lang/String;")
    instructions('\n'.code.toLong()())
}
