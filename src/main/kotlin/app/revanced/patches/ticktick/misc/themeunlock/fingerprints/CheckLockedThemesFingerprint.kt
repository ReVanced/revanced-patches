package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckLockedThemesFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("Theme;") && methodDef.name == "isLockedTheme"
    }
)
