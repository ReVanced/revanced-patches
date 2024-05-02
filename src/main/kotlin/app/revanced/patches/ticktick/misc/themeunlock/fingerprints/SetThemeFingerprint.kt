package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setThemeFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("ThemePreviewActivity;") && methodDef.name == "lambda\$updateUserBtn\$1"
    }
}
