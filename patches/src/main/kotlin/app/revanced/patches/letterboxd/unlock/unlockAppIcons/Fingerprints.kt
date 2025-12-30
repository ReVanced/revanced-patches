package app.revanced.patches.letterboxd.unlock.unlockAppIcons

import app.revanced.patcher.fingerprint

internal val getCanChangeAppIconFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getCanChangeAppIcon" && classDef.type.endsWith("SettingsAppIconFragment;")
    }
}
