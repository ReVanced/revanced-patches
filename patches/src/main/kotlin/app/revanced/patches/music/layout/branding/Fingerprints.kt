package app.revanced.patches.music.layout.branding

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val BytecodePatchContext.cairoSplashAnimationConfigMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    returnType("V")
    definingClass(YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE)
    parameterTypes("Landroid/os/Bundle;")
    instructions(ResourceType.LAYOUT("main_activity_launch_animation"))
}
