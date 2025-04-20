package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val androidShareSheetUrlFormatterFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters("L", "Ljava/lang/String;")
    literal {
        '\n'.code.toLong()
    }
}

internal val copyUrlFormatterFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    strings("clipboard", "Spotify Link")
}
