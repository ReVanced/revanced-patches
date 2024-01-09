package app.revanced.patches.youtube.layout.searchbar.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object CreateSearchSuggestionsFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4
    ),
    strings = listOf("ss_rds")
)