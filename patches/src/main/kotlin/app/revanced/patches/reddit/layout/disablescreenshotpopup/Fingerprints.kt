package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.disableScreenshotPopupMethod by gettingFirstMethodDeclaratively {
    name("invoke")
    definingClass { endsWith($$"$ScreenshotTakenBannerKt$lambda-1$1;") }
    returnType("V")
    parameterTypes("Landroidx/compose/runtime/", "I")
}
