package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.StringComparisonType
import app.revanced.patcher.addString
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 20.02+
 */
internal val userWasInShortsAlternativeFingerprint = fingerprint {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        checkCast("Ljava/lang/Boolean;"),
        methodCall(smali = "Ljava/lang/Boolean;->booleanValue()Z", location = MatchAfterImmediately()),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        // 20.40+ string was merged into another string and is a partial match.
        addString("userIsInShorts: ", comparison = StringComparisonType.CONTAINS, location = MatchAfterWithin(15)),
    )
}

/**
 * Pre 20.02
 */
internal val userWasInShortsLegacyFingerprint = fingerprint {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        addString("Failed to read user_was_in_shorts proto after successful warmup"),
    )
}

/**
 * 18.15.40+
 */
internal val userWasInShortsConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45358360L(),
    )
}
