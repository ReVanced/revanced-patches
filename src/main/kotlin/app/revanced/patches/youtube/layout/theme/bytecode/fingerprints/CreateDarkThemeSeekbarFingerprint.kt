package app.revanced.patches.youtube.layout.theme.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.theme.bytecode.fingerprints.CreateDarkThemeSeekbarFingerprint.indexOfInstructionWithSeekbarId
import app.revanced.patches.youtube.layout.theme.resource.ThemeResourcePatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object CreateDarkThemeSeekbarFingerprint : MethodFingerprint(
    access = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { method -> method.indexOfInstructionWithSeekbarId != -1 },
) {
    /**
     * The index of the instruction that loads the resource id of the seekbar.
     */
    internal val Method.indexOfInstructionWithSeekbarId
        get() = implementation?.let {
            it.instructions.indexOfFirst { instruction ->
                instruction.opcode == Opcode.CONST && (instruction as WideLiteralInstruction).wideLiteral == ThemeResourcePatch.inlineTimeBarColorizedBarPlayedColorDarkId
            }
        }
}