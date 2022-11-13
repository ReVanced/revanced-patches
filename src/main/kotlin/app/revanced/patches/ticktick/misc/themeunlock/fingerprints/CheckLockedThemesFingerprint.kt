package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.ticktick.misc.themeunlock.annotations.UnlockThemesCompatibility

@Name("check-locked-theme-fingerprint")
@UnlockThemesCompatibility
@Version("0.0.1")
object CheckLockedThemesFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("Theme;") && methodDef.name == "isLockedTheme"
    }
)
