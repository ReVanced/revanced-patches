package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object SetThemeFingerprint : MethodFingerprint(
    returnType = "L",
    opcodes = listOf(Opcode.RETURN_OBJECT),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.CONST) return@any false

            val wideLiteral = (instruction as WideLiteralInstruction).wideLiteral

            SettingsResourcePatch.appearanceStringId == wideLiteral
        } ?: false
    }
)