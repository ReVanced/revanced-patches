package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object ReadComponentIdentifierFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IF_NEZ,
        null,
        Opcode.MOVE_RESULT_OBJECT // Register stores the component identifier string
    )
)