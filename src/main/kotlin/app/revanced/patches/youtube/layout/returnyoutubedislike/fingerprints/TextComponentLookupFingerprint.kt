package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves against the same class that [textComponentConstructorFingerprint] resolves to.
 */
internal val textComponentLookupFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    strings("…")
}
