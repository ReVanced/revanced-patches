package app.revanced.patches.reddit.customclients.syncforreddit.detection.piracy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

object PiracyDetectionFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL
    ),
    customFingerprint = { method, _ ->
        method.implementation?.instructions?.any {
            if (it.opcode != Opcode.NEW_INSTANCE) return@any false

            val reference = (it as ReferenceInstruction).reference

            reference.toString() == "Lcom/github/javiersantos/piracychecker/PiracyChecker;"
        } ?: false
    }
)