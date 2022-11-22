package app.revanced.patches.youtube.layout.fullscreenpanels.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object FullscreenViewAdderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_BOOLEAN
    )
)
