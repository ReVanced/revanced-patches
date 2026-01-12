package app.revanced.patches.strava.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationOnCreateHook = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/StravaApplication;")
    }
}
