package app.revanced.patches.youtube.layout.hide.fullscreenambientmode.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeAmbientModeFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    opcodes(Opcode.MOVE_RESULT)
    literal { 45389368 }
}
