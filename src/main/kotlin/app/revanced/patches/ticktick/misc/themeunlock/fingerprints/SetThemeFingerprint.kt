package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.ticktick.misc.themeunlock.annotations.UnlockThemesCompatibility

@Name("set-theme-fingerprint")
@UnlockThemesCompatibility
@Version("0.0.1")
object SetThemeFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("ThemePreviewActivity;") && methodDef.name == "lambda\$updateUserBtn\$1"
    }
)
