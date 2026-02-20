package app.revanced.patches.gamehub.ui.tabs

import app.revanced.patcher.fingerprint

internal val initViewFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/ui/main/LandscapeLauncherMainActivity;" &&
                method.name == "initView"
    }
}
