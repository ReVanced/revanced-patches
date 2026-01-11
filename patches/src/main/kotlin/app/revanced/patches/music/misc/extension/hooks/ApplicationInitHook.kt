package app.revanced.patches.music.misc.extension.hooks

import app.revanced.patcher.*
import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    name("onCreate")
    returnType("V")
    parameterTypes()
    instructions("activity"())
}

internal val applicationInitOnCreateHook = activityOnCreateExtensionHook(YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE)
