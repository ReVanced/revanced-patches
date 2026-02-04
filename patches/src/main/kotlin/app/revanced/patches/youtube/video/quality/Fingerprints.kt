package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.videoQualityItemOnClickParentMethod by gettingFirstImmutableMethodDeclaratively(
    "VIDEO_QUALITIES_MENU_BOTTOM_SHEET_FRAGMENT",
) {
    returnType("V")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getVideoQualityItemOnClickMethod() = firstMethodDeclaratively {
    name("onItemClick")
    returnType("V")
    parameterTypes(
        "Landroid/widget/AdapterView;",
        "Landroid/view/View;",
        "I",
        "J",
    )
}

internal val BytecodePatchContext.videoQualityMenuOptionsMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.STATIC)
    returnType("[L")
    parameterTypes("Landroid/content/Context", "L", "L")
    opcodes(
        Opcode.CONST_4, // First instruction of method.
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN, // Use the quality menu, that contains the advanced menu.
        Opcode.IF_NEZ,
    )
    literal { videoQualityQuickMenuAdvancedMenuDescription }
}

internal val BytecodePatchContext.videoQualityMenuViewInflateMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L", "L", "L")
    opcodes(
        Opcode.INVOKE_SUPER,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { videoQualityBottomSheetListFragmentTitle }
}
