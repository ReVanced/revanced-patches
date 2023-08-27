package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object ConvertElementToFlatBufferFingerprint : MethodFingerprint(
    strings = listOf("Failed to convert Element to Flatbuffers: %s"),
    opcodes = listOf(Opcode.IGET_OBJECT) // Patched at this opcodes index
)