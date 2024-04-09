package app.revanced.patches.amazon.deeplinking

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object DeepLinkingFingerprint : MethodFingerprint(
    "Z",
    parameters = listOf("L"),
    accessFlags = AccessFlags.PRIVATE.value,
    strings = listOf("https://www.", "android.intent.action.VIEW")
)
