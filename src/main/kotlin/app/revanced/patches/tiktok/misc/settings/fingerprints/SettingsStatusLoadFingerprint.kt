package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SettingsStatusLoadFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("Lapp/revanced/tiktok/settingsmenu/SettingsStatus;") &&
                methodDef.name == "load"
    }
)