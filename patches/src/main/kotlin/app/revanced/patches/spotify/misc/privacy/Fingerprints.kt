package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val shareCopyUrlFingerprint by fingerprint {
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    strings("clipboard", "Spotify Link")
    custom { method, _ ->
        method.name == "invokeSuspend"
    }
}

internal val shareCopyUrlLegacyFingerprint by fingerprint {
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    strings("clipboard", "createNewSession failed")
    custom { method, _ ->
        method.name == "apply"
    }
}

internal val formatAndroidShareSheetUrlFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters("L", "Ljava/lang/String;")
    literal {
        '\n'.code.toLong()
    }
}

internal val formatAndroidShareSheetUrlLegacyFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/lang/String;")
    parameters("Lcom/spotify/share/social/sharedata/ShareData;", "Ljava/lang/String;")
    literal {
        '\n'.code.toLong()
    }
}
