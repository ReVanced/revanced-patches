package app.revanced.patches.youtube.misc.fix.cairo

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Added in YouTube v19.04.38.
 *
 * When this value is true, Cairo Fragment is used.
 * In this case, some of the patches may be broken, so set this value to FALSE.
 */
internal val cairoFragmentConfigFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(45532100L),
        opcode(Opcode.MOVE_RESULT, 10)
    )
}
