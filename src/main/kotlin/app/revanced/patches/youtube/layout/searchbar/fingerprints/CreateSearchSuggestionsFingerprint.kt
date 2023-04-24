package app.revanced.patches.youtube.layout.searchbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object CreateSearchSuggestionsFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4
    ),
    strings = listOf("ss_rds")
)