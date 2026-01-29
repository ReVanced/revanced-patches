package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE

internal val BytecodePatchContext.useGradientLoadingScreenMethodMatch by composingFirstMethod {
    instructions(45412406L())
}

internal val BytecodePatchContext.splashScreenStyleMethodMatch by composingFirstMethod {
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    name("onCreate")
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        anyOf(
            1074339245L(), // 20.30+
            269032877L(), // 20.29 and lower.
        ),
    )
}
