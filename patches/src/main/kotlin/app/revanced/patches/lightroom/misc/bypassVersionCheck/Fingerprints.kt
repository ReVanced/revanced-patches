package app.revanced.patches.lightroom.misc.bypassVersionCheck

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.refreshRemoteConfigurationMethod by gettingFirstMutableMethodDeclaratively(
    "com.adobe.lrmobile.denylisted_version_set_key",
    "com.adobe.lrmobile.app_min_version_key"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET, // Overwrite this instruction to disable the check.
    )
}
