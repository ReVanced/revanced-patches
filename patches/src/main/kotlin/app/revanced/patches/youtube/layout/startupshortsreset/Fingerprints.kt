package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 20.02+
 */
internal val userWasInShortsAlternativeFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    instructions(
        checkCast("Ljava/lang/Boolean;"),
        methodCall(smali = "Ljava/lang/Boolean;->booleanValue()Z", maxAfter = 0),
        opcode(Opcode.MOVE_RESULT, maxAfter = 0),
        string("userIsInShorts: ", maxAfter = 5)
    )
}

/**
 * Pre 20.02
 */
internal val userWasInShortsLegacyFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    instructions(
        string("Failed to read user_was_in_shorts proto after successful warmup")
    )
}

/**
 * 18.15.40+
 */
internal val userWasInShortsConfigFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45358360L)
    )
}
