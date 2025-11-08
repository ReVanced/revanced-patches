package app.revanced.patches.meta.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val adInjectorFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Z")
    parameters("L", "L")
    strings(
        "SponsoredContentController.insertItem",
    )
}
