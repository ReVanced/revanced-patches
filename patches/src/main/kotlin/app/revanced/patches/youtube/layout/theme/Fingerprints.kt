package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE

internal val BytecodePatchContext.useGradientLoadingScreenMethod by gettingFirstMutableMethodDeclaratively {
    instructions(
        45412406L(),
    )
}

internal val BytecodePatchContext.splashScreenStyleMethod by gettingFirstMutableMethodDeclaratively {
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        anyInstruction(
            1074339245L(), // 20.30+
            269032877L(), // 20.29 and lower.
        ),
    )
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    name("onCreate")
}
