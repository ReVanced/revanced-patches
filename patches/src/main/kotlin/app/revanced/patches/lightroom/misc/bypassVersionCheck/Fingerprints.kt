package app.revanced.patches.lightroom.misc.version

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val refreshRemoteConfigurationFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    strings(
        "com.adobe.lrmobile.denylisted_version_set_key",
        "com.adobe.lrmobile.app_min_version_key"
    )
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET, // Overwrite this instruction to disable the check.
    )
}
