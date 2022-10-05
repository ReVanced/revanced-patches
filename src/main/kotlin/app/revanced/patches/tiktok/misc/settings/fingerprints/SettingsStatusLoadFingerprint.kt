package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.settings.annotations.TikTokSettingsCompatibility

@Name("settings-status-load-fingerprint")
@TikTokSettingsCompatibility
@Version("0.0.1")
object SettingsStatusLoadFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("Lapp/revanced/tiktok/settingsmenu/SettingsStatus;") &&
                methodDef.name == "load"
    }
)