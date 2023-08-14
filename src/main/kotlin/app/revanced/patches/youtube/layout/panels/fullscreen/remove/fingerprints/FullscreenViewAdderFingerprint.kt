package app.revanced.patches.youtube.layout.panels.fullscreen.remove.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object FullscreenViewAdderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_BOOLEAN
    )
)
