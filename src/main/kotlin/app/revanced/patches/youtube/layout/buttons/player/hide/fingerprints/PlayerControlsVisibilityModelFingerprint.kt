package app.revanced.patches.youtube.layout.buttons.player.hide.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object PlayerControlsVisibilityModelFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.INVOKE_DIRECT_RANGE),
    strings = listOf("Missing required properties:", "hasNext", "hasPrevious")
)