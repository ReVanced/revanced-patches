package app.revanced.patches.music.utils.integrations.fingerprints

import app.revanced.patches.shared.patch.integrations.AbstractIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.Opcode

object InitFingerprint : IntegrationsFingerprint(
    returnType = "V",
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_STATIC,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL
    ),
    strings = listOf("activity"),
    customFingerprint = { methodDef, _ -> methodDef.name == "onCreate" }
)