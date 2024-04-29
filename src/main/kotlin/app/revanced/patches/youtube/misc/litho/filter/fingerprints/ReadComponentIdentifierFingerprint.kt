package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val readComponentIdentifierFingerprint = methodFingerprint {
    opcodes(
        Opcode.IF_NEZ,
        null,
        Opcode.MOVE_RESULT_OBJECT, // Register stores the component identifier string
    )
}
