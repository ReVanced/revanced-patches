package app.revanced.patches.youtube.misc.fix.backtoexitgesture.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object OnBackPressedFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    opcodes = listOf(
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("WatchWhileActivity;")
        && methodDef.name == "onBackPressed"
    }
)