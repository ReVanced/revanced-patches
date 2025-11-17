package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val toolBarButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        resourceLiteral(ResourceType.ID, "menu_item_view"),
        methodCall(returnType = "I", opcode = Opcode.INVOKE_INTERFACE),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
        fieldAccess(type = "Landroid/widget/ImageView;", opcode = Opcode.IGET_OBJECT, location = MatchAfterWithin(6)),
        methodCall("Landroid/content/res/Resources;", "getDrawable", location = MatchAfterWithin(8)),
        methodCall("Landroid/widget/ImageView;", "setImageDrawable", location = MatchAfterWithin(4))
    )
    custom { method, _ ->
        // 20.37+ has second parameter of "Landroid/content/Context;"
        val parameterCount = method.parameterTypes.count()
        (parameterCount == 1 || parameterCount == 2)
                && method.parameterTypes.firstOrNull() == "Landroid/view/MenuItem;"
    }
}

internal val spoofAppVersionFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L")
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
