package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.infocardsIncognitoMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Boolean;")
    parameterTypes("L", "J")
    instructions(
        addString("vibrator"),
    )
}

internal val BytecodePatchContext.infocardsIncognitoParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    instructions(
        addString("player_overlay_info_card_teaser"),
    )
}

internal val BytecodePatchContext.infocardsMethodCallMethod by gettingFirstMethodDeclaratively {
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
    strings("Missing ControlsOverlayPresenter for InfoCards to work.")
    literal { drawerResourceId }
}
