package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val shareCopyUrlFingerprint = fingerprint {
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    strings("clipboard", "Spotify Link")
    custom { method, _ ->
        method.name == "invokeSuspend"
    }
}

internal val oldShareCopyUrlFingerprint = fingerprint {
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    strings("clipboard", "createNewSession failed")
    custom { method, _ ->
        method.name == "apply"
    }
}

internal val formatAndroidShareSheetUrlFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    parameters("L", "Ljava/lang/String;")
    opcodes(
        Opcode.GOTO,
        Opcode.IF_EQZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    )
    literal {
        '\n'.code.toLong()
    }
}

internal val oldFormatAndroidShareSheetUrlFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/lang/String;")
    parameters("Lcom/spotify/share/social/sharedata/ShareData;", "Ljava/lang/String;")
    literal {
        '\n'.code.toLong()
    }
}
