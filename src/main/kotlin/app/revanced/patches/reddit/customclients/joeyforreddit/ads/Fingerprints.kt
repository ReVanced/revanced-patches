package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val isAdFreeUserFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("AD_FREE_USER")
}