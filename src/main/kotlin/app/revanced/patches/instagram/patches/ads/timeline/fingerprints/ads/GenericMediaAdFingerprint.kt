package app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads

import org.jf.dexlib2.Opcode

object GenericMediaAdFingerprint : MediaAdFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.RETURN,
    )
) {
    override fun toString() = result!!.method.toString()
}