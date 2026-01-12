package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal val checkCertificateFingerprint = fingerprint {
    returns("Z")
    parameters("Ljava/lang/String;")
    strings(
        "X509",
        "Failed to get certificate" // Partial String match.
    )
}

internal val enableFullSearchAndroidAutoFingerprint = fingerprint {
    parameters()
    custom { method, classDef ->
        classDef.methods.any { m ->
            m.name == "<init>" && m.implementation?.instructions?.any { instr ->
                instr is ReferenceInstruction &&
                instr.reference.let { ref ->
                    ref is StringReference && ref.string == "ytm_media_browser/search_media_items"
                }
            } == true
        }
    }
}