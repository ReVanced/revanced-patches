package app.revanced.patches.music.misc.extension.hooks

import app.revanced.patcher.string
import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    returns("V")
    parameters()
    instructions(
        string("activity")
    )
    custom { method, _ -> method.name == "onCreate" }
}

internal val applicationInitOnCreateHook = activityOnCreateExtensionHook(
    YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
)
