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

internal val startupPageLayoutInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    strings("blueprintContainer", "gradient", "valuePropositionTextView")
}

internal val loginSetListenerFingerprint = fingerprint {
    strings("EMAIL_OR_USERNAME", "listener")
}

internal val loginOnClickFingerprint = fingerprint {
    strings("login", "listener", "none")
}

internal val firstLoginScreenFingerprint = fingerprint {
    strings("authenticationButtonFactory", "intent_login")
}