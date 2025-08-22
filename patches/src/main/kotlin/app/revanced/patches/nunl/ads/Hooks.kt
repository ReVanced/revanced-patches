package app.revanced.patches.nunl.ads

import app.revanced.patches.shared.misc.extension.extensionHook

internal val mainActivityOnCreateHook = extensionHook {
    custom { method, classDef ->
        classDef.endsWith("/NUApplication;") && method.name == "onCreate"
    }
}
