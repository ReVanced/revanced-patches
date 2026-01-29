package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getInfocardsIncognitoMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Boolean;")
    parameterTypes("L", "J")
    instructions("vibrator"())
}

internal val BytecodePatchContext.infocardsIncognitoParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    instructions(
        "player_overlay_info_card_teaser"(),
    )
}

internal val BytecodePatchContext.infocardsMethodCallMethodMatch by
    composingFirstMethod("Missing ControlsOverlayPresenter for InfoCards to work.") {
        opcodes(
            Opcode.INVOKE_VIRTUAL,
            Opcode.IGET_OBJECT,
            Opcode.INVOKE_INTERFACE,
        )
        literal { drawerResourceId }
    }
