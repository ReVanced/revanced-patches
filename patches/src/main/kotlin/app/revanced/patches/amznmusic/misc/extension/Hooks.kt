package app.revanced.patches.amznmusic.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/MusicHomeActivity;")
    }
}
