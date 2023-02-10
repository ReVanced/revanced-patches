package app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads

import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

object PaidPartnershipAdFingerprint : MediaAdFingerprint(
    "V",
    null,
    listOf("L", "L"),
    listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_BOOLEAN
    ),
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("ClipsEditMetadataController;")
    }
) {
    override fun toString() = result!!.let {
        val adCheckIndex = it.scanResult.patternScanResult!!.startIndex
        val adCheckInstruction = it.method.implementation!!.instructions.elementAt(adCheckIndex)

        val adCheckMethod = (adCheckInstruction as ReferenceInstruction).reference as MethodReference

        adCheckMethod.toString()
    }
}