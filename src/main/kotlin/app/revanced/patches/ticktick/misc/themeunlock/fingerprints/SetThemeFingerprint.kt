package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SetThemeFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("ThemePreviewActivity;") && methodDef.name == "lambda\$updateUserBtn\$1"
    }
)
