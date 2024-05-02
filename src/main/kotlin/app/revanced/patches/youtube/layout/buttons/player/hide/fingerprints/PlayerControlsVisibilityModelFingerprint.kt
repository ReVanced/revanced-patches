package app.revanced.patches.youtube.layout.buttons.player.hide.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val playerControlsVisibilityModelFingerprint = methodFingerprint {
    opcodes(Opcode.INVOKE_DIRECT_RANGE)
    strings("Missing required properties:", "hasNext", "hasPrevious")
}
