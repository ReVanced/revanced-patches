package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.spoofAppVersionMethodMatch by composingFirstMethod(
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
