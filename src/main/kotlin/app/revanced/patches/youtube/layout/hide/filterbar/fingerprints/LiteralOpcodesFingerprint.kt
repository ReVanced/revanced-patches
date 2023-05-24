package app.revanced.patches.youtube.layout.hide.filterbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction


abstract class LiteralOpcodesFingerprint(opcodes: List<Opcode>, literal: Long) : MethodFingerprint(
    opcodes = opcodes,
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.CONST) return@any false

            val wideLiteral = (instruction as WideLiteralInstruction).wideLiteral

            literal == wideLiteral
        } ?: false
    }
)