package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val hideCommentAdsFingerprint = fingerprint {
    returns("V")
    custom { method, _ ->
        method.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.NEW_INSTANCE) return@any false
            val reference = (instruction as ReferenceInstruction).reference as TypeReference
            reference.type.contains("LoadAdsCombinedCall")
        } == true
    }
}
