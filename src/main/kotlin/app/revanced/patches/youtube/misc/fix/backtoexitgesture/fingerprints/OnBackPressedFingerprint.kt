package app.revanced.patches.youtube.misc.fix.backtoexitgesture.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object OnBackPressedFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    opcodes = listOf(
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef, _ ->
        (methodDef.definingClass.endsWith("MainActivity;") ||
                // Old versions of YouTube called this class "WatchWhileActivity" instead.
                methodDef.definingClass.endsWith("WatchWhileActivity;"))
        && methodDef.name == "onBackPressed"
    }
)