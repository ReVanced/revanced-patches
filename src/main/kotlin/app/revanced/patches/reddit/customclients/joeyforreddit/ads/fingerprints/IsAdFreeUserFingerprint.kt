package app.revanced.patches.reddit.customclients.joeyforreddit.ads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isAdFreeUserFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("AD_FREE_USER")
}