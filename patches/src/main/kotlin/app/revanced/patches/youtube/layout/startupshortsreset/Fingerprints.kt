package app.revanced.patches.youtube.layout.startupshortsreset

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 20.02+
 */
internal val userWasInShortsAlternativeMethodMatch = firstMethodComposite {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Ljava/lang/Object;")
    instructions(
        allOf(Opcode.CHECK_CAST(), type("Ljava/lang/Boolean;")),
        after(method { toString() == "Ljava/lang/Boolean;->booleanValue()Z" }),
        after(Opcode.MOVE_RESULT()),
        // 20.40+ string was merged into another string and is a partial match.
        afterAtMost(15, "userIsInShorts: "(String::contains)),
    )
}

/**
 * Pre 20.02
 */
internal val BytecodePatchContext.userWasInShortsLegacyMethod by gettingFirstMutableMethodDeclaratively {
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
internal val BytecodePatchContext.userWasInShortsConfigMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45358360L(),
    )
}
