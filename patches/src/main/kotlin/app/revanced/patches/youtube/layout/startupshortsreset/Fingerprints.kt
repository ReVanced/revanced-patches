package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.StringComparisonType
import app.revanced.patcher.accessFlags
import app.revanced.patcher.checkCast
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 20.02+
 */
internal val BytecodePatchContext.userWasInShortsAlternativeMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        checkCast("Ljava/lang/Boolean;"),
        methodCall(smali = "Ljava/lang/Boolean;->booleanValue()Z", after()),
        after(Opcode.MOVE_RESULT()),
        // 20.40+ string was merged into another string and is a partial match.
        addString("userIsInShorts: ", comparison = StringComparisonType.CONTAINS, afterAtMost(15)),
    )
}

/**
 * Pre 20.02
 */
internal val BytecodePatchContext.userWasInShortsLegacyMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        "Failed to read user_was_in_shorts proto after successful warmup"(),
    )
}

/**
 * 18.15.40+
 */
internal val BytecodePatchContext.userWasInShortsConfigMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45358360L(),
    )
}
