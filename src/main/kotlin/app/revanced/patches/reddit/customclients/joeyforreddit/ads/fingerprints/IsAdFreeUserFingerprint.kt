package app.revanced.patches.reddit.customclients.joeyforreddit.ads.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object IsAdFreeUserFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    strings = listOf("AD_FREE_USER")
)