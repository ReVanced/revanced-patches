package app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.resource.patch.HideShortsComponentsResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object CreateShortsButtonsFingerprint : MethodFingerprint(
    parameters = listOf("Z", "Z", "L"),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any {
            if (it.opcode != Opcode.CONST) return@any false

            val literal = (it as WideLiteralInstruction).wideLiteral

            literal == HideShortsComponentsResourcePatch.reelPlayerRightLargeIconSize
        } ?: false
    }
)