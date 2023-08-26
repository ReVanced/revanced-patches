package app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object BottomNavigationBarFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT, // Refers to bottom navigation bar
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
    ),
    strings = listOf(
        "navigation_endpoint_interaction_logging_extension",
        "reel_watch_fragment_watch_while",
    ),
)