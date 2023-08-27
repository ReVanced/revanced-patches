package app.revanced.patches.songpal.badge.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.songpal.badge.patch.BadgeTabPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Located @ ub.i0.h#p (9.5.0)
object CreateTabsFingerprint : MethodFingerprint(
    "Ljava/util/List",
    accessFlags = AccessFlags.PRIVATE.value,
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.INVOKE_STATIC) return@any false

            val reference = (instruction as ReferenceInstruction).reference as MethodReference

            if (reference.parameterTypes.isNotEmpty()) return@any false
            if (reference.definingClass != BadgeTabPatch.ACTIVITY_TAB_DESCRIPTOR) return@any false
            if (reference.returnType != "[${BadgeTabPatch.ACTIVITY_TAB_DESCRIPTOR}") return@any false
            true
        } ?: false
    }
)
