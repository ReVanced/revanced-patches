package app.revanced.patches.youtube.misc.fix.backtoexitgesture.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val onBackPressedFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    opcodes(Opcode.RETURN_VOID)
    custom { method, classDef ->
        method.name == "onBackPressed" &&
            // Old versions of YouTube called this class "WatchWhileActivity" instead.
            (classDef.endsWith("MainActivity;") || classDef.endsWith("WatchWhileActivity;"))
    }
}
