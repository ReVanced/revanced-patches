package app.revanced.patches.reddit.layout.disablescreenshotpopup.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val disableScreenshotPopupFingerprint = methodFingerprint {
    returns("V")
    parameters("Landroidx/compose/runtime/", "I")
    custom custom@{ methodDef, classDef ->
        if (!classDef.endsWith("\$ScreenshotTakenBannerKt\$lambda-1\$1;"))
            return@custom false

        methodDef.name == "invoke"
    }
}