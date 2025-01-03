package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val userWasInShortsFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    strings("Failed to read user_was_in_shorts proto after successful warmup")
}

/**
 * 18.15.40+
 */
internal val userWasInShortsConfigFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal {
        45358360L
    }
}
