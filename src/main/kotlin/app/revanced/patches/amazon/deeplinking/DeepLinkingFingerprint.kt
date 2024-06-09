package app.revanced.patches.amazon.deeplinking

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val deepLinkingFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Z")
    parameters("L")
    strings("https://www.", "android.intent.action.VIEW")
}