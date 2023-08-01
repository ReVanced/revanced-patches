package app.revanced.patches.youtube.misc.playertype.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object PlayerTypeFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IF_NE,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/YouTubePlayerOverlaysLayout;") }
)
