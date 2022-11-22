package app.revanced.patches.youtube.misc.video.speed.custom.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object VideoSpeedPatchFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.FILL_ARRAY_DATA),
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("CustomVideoSpeedPatch;") && methodDef.name == "<clinit>"
    }
)
