package app.revanced.patches.lightroom.misc.version

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val versionCheckFingerprint = fingerprint {
    // Matches public static k()V
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)

    strings(
        "com.adobe.lrmobile.denylisted_version_set_key",
        "com.adobe.lrmobile.app_min_version_key"
    )

    opcodes(
        Opcode.INVOKE_STATIC,       // invoke-static {}, Lbf/p;->b()Lbf/p$a;
        Opcode.MOVE_RESULT_OBJECT,  // move-result-object v0
        Opcode.IGET,                // iget v1, v0, Lbf/p$a;->a:I  <-- TARGET
        Opcode.CONST_4,             // const/4 v2, -0x2
        Opcode.CONST_STRING,        // const-string v3...
        Opcode.CONST_STRING,        // const-string v4...
        Opcode.IF_NE                // if-ne v1, v2, :cond_0
    )
}
