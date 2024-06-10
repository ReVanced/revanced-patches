package app.revanced.patches.reddit.customclients.sync.detection.piracy.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal val piracyDetectionFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
    )
    custom { method, _ ->
        method.implementation?.instructions?.any {
            if (it.opcode != Opcode.NEW_INSTANCE) return@any false

            val reference = (it as ReferenceInstruction).reference

            reference.toString() == "Lcom/github/javiersantos/piracychecker/PiracyChecker;"
        } ?: false
    }
}
