package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkLockedThemesFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("Theme;") && methodDef.name == "isLockedTheme"
    }
}

internal val setThemeFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("ThemePreviewActivity;") && methodDef.name == "lambda\$updateUserBtn\$1"
    }
}
