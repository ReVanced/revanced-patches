package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * YouTube 20.02.08 ~
 */
internal val userWasInShortsAlternativeFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    strings("userIsInShorts: ")
}

internal val userWasInShortsLegacyFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    strings("Failed to read user_was_in_shorts proto after successful warmup")
}

/**
 * 18.15.40+
 */
internal val userWasInShortsConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal {
        45358360L
    }
}
