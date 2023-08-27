package app.revanced.patches.youtube.layout.panels.fullscreen.remove.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object FullscreenViewAdderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_BOOLEAN
    )
)
