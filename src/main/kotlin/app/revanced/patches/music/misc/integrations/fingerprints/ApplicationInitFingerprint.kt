package app.revanced.patches.music.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ApplicationInitFingerprint : IntegrationsFingerprint(
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
    customFingerprint = { methodDef, _ -> methodDef.name == "onCreate" },
)
