package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.accessFlags
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.toolBarButtonMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        ResourceType.ID("menu_item_view"),
        methodCall(returnType = "I", opcode = Opcode.INVOKE_INTERFACE),
        after(Opcode.MOVE_RESULT()),
        fieldAccess(type = "Landroid/widget/ImageView;", opcode = Opcode.IGET_OBJECT, afterAtMost(6)),
        methodCall("Landroid/content/res/Resources;", "getDrawable", afterAtMost(8)),
        methodCall("Landroid/widget/ImageView;", "setImageDrawable", afterAtMost(4)),
    )
    custom { method, _ ->
        // 20.37+ has second parameter of "Landroid/content/Context;"
        val parameterCount = method.parameterTypes.count()
        (parameterCount == 1 || parameterCount == 2) &&
            method.parameterTypes.firstOrNull() == "Landroid/view/MenuItem;"
    }
}

internal val BytecodePatchContext.spoofAppVersionMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    parameterTypes("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.GOTO,
        Opcode.CONST_STRING,
    )
    // Instead of applying a bytecode patch, it might be possible to only rely on code from the extension and
    // manually set the desired version string as this keyed value in the SharedPreferences.
    // But, this bytecode patch is simple and it works.
    strings("pref_override_build_version_name")
}
