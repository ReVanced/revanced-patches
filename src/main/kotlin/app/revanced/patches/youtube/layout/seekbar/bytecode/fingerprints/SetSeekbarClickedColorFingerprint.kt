package app.revanced.patches.youtube.layout.seekbar.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object SetSeekbarClickedColorFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.CONST_HIGH16),
    strings = listOf("YOUTUBE", "PREROLL", "POSTROLL"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("ControlsOverlayStyle;")
    }
)