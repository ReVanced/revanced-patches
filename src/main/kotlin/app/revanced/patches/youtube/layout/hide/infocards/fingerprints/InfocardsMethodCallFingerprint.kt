package app.revanced.patches.youtube.layout.hide.infocards.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.infocards.drawerResourceId
import com.android.tools.smali.dexlib2.Opcode

internal val infocardsMethodCallFingerprint = methodFingerprint(
    literal { drawerResourceId },
) {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
    strings("Missing ControlsOverlayPresenter for InfoCards to work.")
}
