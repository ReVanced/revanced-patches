package app.revanced.patches.youtube.layout.hide.fullscreenambientmode

import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val initializeAmbientModeFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    opcodes(Opcode.MOVE_RESULT)
    literal { 45389368 }
}
