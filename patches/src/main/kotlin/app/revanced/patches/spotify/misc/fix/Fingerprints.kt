package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getPackageInfoFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures"
    )
}

internal val startLiborbitFingerprint = fingerprint {
    strings("/liborbit-jni-spotify.so")
}

internal val clientTokenSuccessClassFingerprint = fingerprint {
    strings("ClientTokenSuccess(clientToken=")
}

internal val clientTokenSuccessConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

internal val startupPageLayoutInflateFIngerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    strings("blueprintContainer", "gradient", "valuePropositionTextView")
}
