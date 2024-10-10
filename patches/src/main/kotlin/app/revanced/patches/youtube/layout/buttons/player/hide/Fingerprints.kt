package app.revanced.patches.youtube.layout.buttons.player.hide

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val playerControlsVisibilityModelFingerprint = fingerprint {
    opcodes(Opcode.INVOKE_DIRECT_RANGE)
    strings("Missing required properties:", "hasNext", "hasPrevious")
}
