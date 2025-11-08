package app.revanced.patches.music.layout.branding

import app.revanced.patcher.fingerprint
import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral

internal val cairoSplashAnimationConfigFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    instructions(
        resourceLiteral(ResourceType.LAYOUT, "main_activity_launch_animation")
    )
    custom { method, classDef ->
        method.name == "onCreate" && method.definingClass == YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
    }
}
