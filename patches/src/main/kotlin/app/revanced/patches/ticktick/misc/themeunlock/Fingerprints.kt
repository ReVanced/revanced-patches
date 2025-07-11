package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.fingerprint

internal val checkLockedThemesFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("Theme;") && method.name == "isLockedTheme"
    }
}

internal val setThemeFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("ThemePreviewActivity;") && method.name == "lambda\$updateUserBtn\$1"
    }
}
