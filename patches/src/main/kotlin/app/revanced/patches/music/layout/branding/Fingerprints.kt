package app.revanced.patches.music.layout.branding

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral

internal val BytecodePatchContext.cairoSplashAnimationConfigMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    returnType("V")
    definingClass(YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE)
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        resourceLiteral(ResourceType.LAYOUT, "main_activity_launch_animation")
    )
}
