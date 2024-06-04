package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkLockedThemesFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("Theme;") && methodDef.name == "isLockedTheme"
    }
}
