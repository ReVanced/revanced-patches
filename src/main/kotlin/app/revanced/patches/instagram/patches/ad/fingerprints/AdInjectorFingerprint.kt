package app.revanced.patches.instagram.patches.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object AdInjectorFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PRIVATE.value,
    parameters = listOf("L", "L"),
    strings = listOf(
        "SponsoredContentController.insertItem",
        "SponsoredContentController::Delivery",
    ),
)
