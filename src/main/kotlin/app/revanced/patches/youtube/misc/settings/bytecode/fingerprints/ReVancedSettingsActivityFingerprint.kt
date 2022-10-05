package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.settings.annotations.SettingsCompatibility

// TODO: This is more of a class fingerprint than a method fingerprint.
//  Convert to a class fingerprint whenever possible.
@Name("revanced-settings-activity-fingerprint")
@SettingsCompatibility
@Version("0.0.1")
object ReVancedSettingsActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("ReVancedSettingActivity;") && methodDef.name == "initializeSettings"
    }
)