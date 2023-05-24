package app.revanced.patches.citra.misc.premium.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PremiumUnlockFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lorg/citra/citra_emu/ui/main/MainActivity;" && methodDef.name == "isPremiumActive"
    }
)