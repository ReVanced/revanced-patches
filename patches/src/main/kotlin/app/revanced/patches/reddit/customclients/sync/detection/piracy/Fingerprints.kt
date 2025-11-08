package app.revanced.patches.reddit.customclients.sync.detection.piracy

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.Reference

internal val piracyDetectionFingerprint = fingerprint {
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
        method.implementation ?: return@custom false
        method.instructions.any {
            it.getReference<Reference>()?.toString() == "Lcom/github/javiersantos/piracychecker/PiracyChecker;"
        }
    }
}
