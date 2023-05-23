package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.interaction.seekbar.patch.EnableSeekbarTappingResourcePatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction


object AccessibilityPlayerProgressTimeFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.CONST) return@any false

            val wideLiteral = (instruction as WideLiteralInstruction).wideLiteral

            EnableSeekbarTappingResourcePatch.accessibilityPlayerProgressTime == wideLiteral
        } ?: false
    }
)