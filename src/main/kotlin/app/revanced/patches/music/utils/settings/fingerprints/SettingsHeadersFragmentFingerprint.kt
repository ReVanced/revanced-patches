package app.revanced.patches.music.utils.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object SettingsHeadersFragmentFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsHeadersFragment;")
                && methodDef.name == "onCreate"
    }
)