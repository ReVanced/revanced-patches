package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint
import app.revanced.patcher.firstMethodBuilder
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.returnType
import app.revanced.patcher.string
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val infocardsIncognitoFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Boolean;")
    parameterTypes("L", "J")
    instructions(
        addString("vibrator"),
    )
}

internal val infocardsIncognitoParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    instructions(
        addString("player_overlay_info_card_teaser"),
    )
}

internal val infocardsMethodCallFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
    strings("Missing ControlsOverlayPresenter for InfoCards to work.")
    literal { drawerResourceId }
}
