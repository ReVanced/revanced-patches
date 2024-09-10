package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

/**
 * In 19.17 and earlier, this resolves to the same method as [ComponentContextParserFingerprint].
 * In 19.18+ this resolves to a different method.
 */
internal object ReadComponentIdentifierFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IF_NEZ,
        null,
        Opcode.MOVE_RESULT_OBJECT // Register stores the component identifier string
    ),
    strings = listOf("Number of bits must be positive")
)