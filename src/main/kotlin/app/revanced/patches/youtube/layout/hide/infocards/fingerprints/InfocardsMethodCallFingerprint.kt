package app.revanced.patches.youtube.layout.hide.infocards.fingerprints

import app.revanced.patches.youtube.layout.hide.infocards.drawerResourceId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val infocardsMethodCallFingerprint = literalValueFingerprint(
    literalSupplier = { drawerResourceId },
) {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
    strings("Missing ControlsOverlayPresenter for InfoCards to work.")
}
