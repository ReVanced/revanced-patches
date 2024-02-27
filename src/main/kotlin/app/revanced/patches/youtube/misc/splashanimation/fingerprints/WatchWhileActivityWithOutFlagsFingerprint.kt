package app.revanced.patches.youtube.misc.splashanimation.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.DarkSplashAnimation
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode

object WatchWhileActivityWithOutFlagsFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    opcodes = listOf(
        Opcode.IF_EQZ, // target
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ // target
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "onCreate"
                && methodDef.containsWideLiteralInstructionIndex(DarkSplashAnimation)
    }
)