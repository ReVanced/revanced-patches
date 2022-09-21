package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.settings.annotations.TikTokSettingsCompatibility

@Name("copyright-settings-string-fingerprint")
@MatchingMethod(
    "Lcom/ss/android/ugc/aweme/setting/ui/SettingNewVersionFragment;",
    "onViewCreated"
)
@TikTokSettingsCompatibility
@Version("0.0.1")
object CopyRightSettingsStringFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/SettingNewVersionFragment;") &&
                methodDef.name == "onViewCreated"
    }
)