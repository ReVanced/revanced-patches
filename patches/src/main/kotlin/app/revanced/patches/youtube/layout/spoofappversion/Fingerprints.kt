package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val toolBarButtonFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/view/MenuItem;")
    custom { method, _ ->
        method.containsLiteralInstruction(menuItemView) &&
                indexOfGetDrawableInstruction(method) >= 0
    }
}

internal fun indexOfGetDrawableInstruction(method: Method) = method.indexOfFirstInstruction {
    val reference = getReference<MethodReference>()
    reference?.definingClass == "Landroid/content/res/Resources;" &&
            reference.name == "getDrawable"
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
