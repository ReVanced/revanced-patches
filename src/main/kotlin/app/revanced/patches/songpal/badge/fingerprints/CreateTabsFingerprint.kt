package app.revanced.patches.songpal.badge.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.songpal.badge.ACTIVITY_TAB_DESCRIPTOR
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Located @ ub.i0.h#p (9.5.0)
internal val createTabsFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Ljava/util/List;")
    custom { methodDef, _ ->
        methodDef.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.INVOKE_STATIC) return@any false

            val reference = (instruction as ReferenceInstruction).reference as MethodReference

            if (reference.parameterTypes.isNotEmpty()) return@any false
            if (reference.definingClass != ACTIVITY_TAB_DESCRIPTOR) return@any false
            if (reference.returnType != "[${ACTIVITY_TAB_DESCRIPTOR}") return@any false
            true
        } ?: false
    }
}
