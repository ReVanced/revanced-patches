package app.revanced.patches.youtube.layout.hide.fullscreenambientmode.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeAmbientModeFingerprint = methodFingerprint(literal { 45389368 }) {
    returns("V")
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    opcodes(Opcode.MOVE_RESULT)
}
