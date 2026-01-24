package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.toolBarButtonMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        ResourceType.ID("menu_item_view"),
        allOf(Opcode.INVOKE_VIRTUAL(), method { returnType == "I" }),
        after(Opcode.MOVE_RESULT()),
        afterAtMost(6, allOf(Opcode.IGET_OBJECT(), field { type == "Landroid/widget/ImageView;" })),
        afterAtMost(8, method { name == "getDrawable" && definingClass == "Landroid/content/res/Resources;" }),
        afterAtMost(4, method { name == "setImageDrawable" && definingClass == "Landroid/widget/ImageView;" }),
    )
    // 20.37+ has second parameter of "Landroid/content/Context;"
    custom { parameterTypes.count() in 1..2 && parameterTypes.first() == "Landroid/view/MenuItem;" }
}

internal val BytecodePatchContext.spoofAppVersionMethod by gettingFirstMethodDeclaratively(
    // Instead of applying a bytecode patch, it might be possible to only rely on code from the extension and
    // manually set the desired version string as this keyed value in the SharedPreferences.
    // But, this bytecode patch is simple and it works.
    "pref_override_build_version_name",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("L")
    parameterTypes("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.GOTO,
        Opcode.CONST_STRING,
    )
}
