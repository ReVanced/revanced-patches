package app.revanced.patches.youtube.layout.hide.fullscreenambientmode.fingerprints

import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeAmbientModeFingerprint = literalValueFingerprint(literalSupplier = { 45389368 }) {
    returns("V")
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    opcodes(Opcode.MOVE_RESULT)
}
