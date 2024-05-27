package app.revanced.patches.youtube.layout.searchbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val createSearchSuggestionsFingerprint = methodFingerprint {
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
    )
    strings("ss_rds")
}
