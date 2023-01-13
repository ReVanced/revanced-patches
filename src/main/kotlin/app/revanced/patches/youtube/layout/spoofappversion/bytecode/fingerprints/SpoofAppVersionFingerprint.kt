package app.revanced.patches.youtube.layout.spoofappversion.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object SpoofAppVersionFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L"), listOf(
        Opcode.IGET_OBJECT,
        Opcode.GOTO,
        Opcode.CONST_STRING,
    ),

    // Instead of applying a bytecode patch, it might be possible to only rely on code from the integrations and
    // manually set the desired version string as this keyed value in the SharedPreferences.
    // But, this bytecode patch is simple and it works.
    strings = listOf("pref_override_build_version_name")
)