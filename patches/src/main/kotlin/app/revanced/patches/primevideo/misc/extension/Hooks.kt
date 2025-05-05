package app.revanced.patches.primevideo.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/SplashScreenActivity;")
    }
}
