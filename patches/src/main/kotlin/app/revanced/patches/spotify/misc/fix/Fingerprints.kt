package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val getPackageInfoFingerprint by fingerprint {
    instructions(
        string("Failed to get the application signatures")
    )
}

internal val startLiborbitFingerprint by fingerprint {
    strings("/liborbit-jni-spotify.so")
}

internal val startupPageLayoutInflateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;")
    strings("blueprintContainer", "gradient", "valuePropositionTextView")
}
