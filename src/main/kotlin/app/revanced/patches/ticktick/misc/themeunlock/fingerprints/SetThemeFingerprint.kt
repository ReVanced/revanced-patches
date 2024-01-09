package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SetThemeFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("ThemePreviewActivity;") && methodDef.name == "lambda\$updateUserBtn\$1"
    }
)
