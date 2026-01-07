package app.revanced.patches.youtube.misc.audiofocus

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val audioFocusChangeListenerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I")
    strings(
        "AudioFocus DUCK",
        "AudioFocus loss; Will lower volume",
    )
}

// Fingerprint for bjk.a() - the builder method that creates AudioFocusRequest wrapper
// This method throws IllegalStateException with this specific message
internal val audioFocusRequestBuilderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")  // Returns an object (bjl)
    parameters()  // No parameters
    strings("Can't build an AudioFocusRequestCompat instance without a listener")
}
