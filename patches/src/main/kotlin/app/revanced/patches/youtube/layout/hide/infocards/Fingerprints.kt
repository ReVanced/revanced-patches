package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val infocardsIncognitoFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/Boolean;")
    parameters("L", "J")
    instructions(
        string("vibrator")
    )
}

internal val infocardsIncognitoParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    instructions(
        string("player_overlay_info_card_teaser")
    )
}

internal val infocardsMethodCallFingerprint by fingerprint {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
    strings("Missing ControlsOverlayPresenter for InfoCards to work.")
    literal { drawerResourceId }
}
