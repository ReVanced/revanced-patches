package app.revanced.patches.music.misc.spoof

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val playerRequestConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("player")
}

/**
 * Matches using the class found in [playerRequestConstructorFingerprint].
 */
internal val createPlayerRequestBodyFingerprint by fingerprint {
    parameters("L")
    returns("V")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
    )
    strings("ms")
}

/**
 * Used to get a reference to other clientInfo fields.
 */
internal val setClientInfoFieldsFingerprint by fingerprint {
    returns("L")
    strings("Google Inc.")
}

/**
 * Used to get a reference to the clientInfo and clientInfo.clientVersion field.
 */
internal val setClientInfoClientVersionFingerprint by fingerprint {
    strings("10.29")
}
