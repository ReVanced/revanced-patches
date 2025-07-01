package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val getPackageInfoFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures"
    )
}

internal val loadOrbitLibraryFingerprint = fingerprint {
    strings("/liborbit-jni-spotify.so")
}

internal val startupPageLayoutInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    strings("blueprintContainer", "gradient", "valuePropositionTextView")
}

internal val runIntegrityVerificationFingerprint = fingerprint {
    returns("V")
    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<FieldReference>()
            ?.type?.endsWith("StandardIntegrityManager${"$"}StandardIntegrityTokenProvider;") == true
        } >= 0 && method.indexOfFirstInstruction {
            getReference<TypeReference>()?.type == "Ljava/security/MessageDigest;"
        } >= 0
    }
}
