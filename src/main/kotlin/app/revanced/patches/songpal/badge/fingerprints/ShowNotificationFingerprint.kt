package app.revanced.patches.songpal.badge.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.songpal.badge.fingerprints.ShowNotificationFingerprint.expectedReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

// Located @ com.sony.songpal.mdr.vim.activity.MdrRemoteBaseActivity.e#run (9.5.0)
object ShowNotificationFingerprint : MethodFingerprint(
    "V",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = custom@{ methodDef, _ ->
        methodDef.implementation?.instructions?.any { instruction ->
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
        } ?: false
    }
) {
    val expectedReference = ImmutableMethodReference(
        "Lcom/google/android/material/bottomnavigation/BottomNavigationView;",
        "getOrCreateBadge", // Non-obfuscated placeholder method name.
        listOf("I"),
        "Lcom/google/android/material/badge/BadgeDrawable;",
    )
}
