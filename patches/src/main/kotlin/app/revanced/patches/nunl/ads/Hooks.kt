package app.revanced.patches.nunl.ads

import app.revanced.patches.shared.misc.extension.extensionHook

internal val mainActivityOnCreateHook = extensionHook {
    custom { method, classDef ->
        classDef.type == "Lnl/sanomamedia/android/nu/main/NUMainActivity;" && method.name == "onCreate"
    }
}
