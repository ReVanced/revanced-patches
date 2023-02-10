package app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads

import org.jf.dexlib2.Opcode

object ShoppingAdFingerprint : MediaAdFingerprint(
    opcodes = listOf(
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.XOR_INT_LIT8,
        Opcode.IF_EQZ,
    )
) {
    override fun toString() = result!!.method.toString()
}