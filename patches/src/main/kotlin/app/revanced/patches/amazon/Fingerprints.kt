package app.revanced.patches.amazon

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val deepLinkingFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Z")
    parameters("L")
    strings("https://www.", "android.intent.action.VIEW")
}
