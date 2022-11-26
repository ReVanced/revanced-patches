package app.revanced.patches.citra.misc.premium.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PremiumUnlockFingerprint : MethodFingerprint(
    customFingerprint = { it.definingClass == "Lorg/citra/citra_emu/ui/main/MainActivity;" && it.name == "isPremiumActive" }
)