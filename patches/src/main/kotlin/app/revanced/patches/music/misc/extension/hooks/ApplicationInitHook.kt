package app.revanced.patches.music.misc.extension.hooks

import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    returns("V")
    parameters()
    strings("activity")
    custom { method, _ -> method.name == "onCreate" }
}

internal val applicationInitOnCreateHook = extensionHook {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
    }
}
