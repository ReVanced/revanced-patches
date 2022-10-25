package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

@Name("theme-setter-system-fingerprint")
@SettingsCompatibility
@Version("0.0.1")
object ThemeSetterSystemFingerprint : MethodFingerprint(
    "L",
    opcodes = listOf(Opcode.RETURN_OBJECT),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any {
            it.opcode.ordinal == Opcode.CONST.ordinal && (it as WideLiteralInstruction).wideLiteral == SettingsResourcePatch.appearanceStringId
        } == true
    }
)