package app.revanced.patches.youtube.layout.hide.infocards.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint
import app.revanced.patches.youtube.layout.hide.infocards.resource.patch.HideInfocardsResourcePatch
import com.android.tools.smali.dexlib2.Opcode

object InfocardsMethodCallFingerprint : LiteralValueFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    ),
    strings = listOf("Missing ControlsOverlayPresenter for InfoCards to work."),
    literal = HideInfocardsResourcePatch.drawerResourceId
)