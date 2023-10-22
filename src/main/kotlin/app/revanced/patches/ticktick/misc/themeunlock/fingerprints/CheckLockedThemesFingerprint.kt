package app.revanced.patches.ticktick.misc.themeunlock.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CheckLockedThemesFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("Theme;") && methodDef.name == "isLockedTheme"
    }
)
