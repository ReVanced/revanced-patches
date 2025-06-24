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

internal val thirdLoginScreenRenderFingerprint = fingerprint {
    strings("EMAIL_OR_USERNAME", "listener")
}

internal val thirdLoginOnClickFingerprint = fingerprint {
    strings("login", "listener", "none")
}

internal val firstLoginScreenRenderFingerprint = fingerprint {
    strings("authenticationButtonFactory", "MORE_OPTIONS")
}

internal val secondLoginScreenRenderFingerprint = fingerprint {
    strings("authenticationButtonFactory", "intent_login")
}