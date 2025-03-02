package app.revanced.patches.songpal.badge

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

// Located @ ub.i0.h#p (9.5.0)
internal val createTabsFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Ljava/util/List;")
    custom { method, _ ->
        method.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.INVOKE_STATIC) return@any false

            val reference = (instruction as ReferenceInstruction).reference as MethodReference

            if (reference.parameterTypes.isNotEmpty()) return@any false
            if (reference.definingClass != ACTIVITY_TAB_DESCRIPTOR) return@any false
            if (reference.returnType != "[${ACTIVITY_TAB_DESCRIPTOR}") return@any false
            true
        } == true
    }
}

// Located @ com.sony.songpal.mdr.vim.activity.MdrRemoteBaseActivity.e#run (9.5.0)
internal val showNotificationFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("V")
    custom { method, _ ->
        method.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return@any false

            with(expectedReference) {
                val currentReference = (instruction as ReferenceInstruction).reference as MethodReference
                currentReference.let {
                    if (it.definingClass != definingClass) return@any false
                    if (it.parameterTypes != parameterTypes) return@any false
                    if (it.returnType != returnType) return@any false
                }
            }
            true
        } == true
    }
}

internal val expectedReference = ImmutableMethodReference(
    "Lcom/google/android/material/bottomnavigation/BottomNavigationView;",
    "getOrCreateBadge", // Non-obfuscated placeholder method name.
    listOf("I"),
    "Lcom/google/android/material/badge/BadgeDrawable;",
)
