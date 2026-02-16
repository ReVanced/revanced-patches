package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.checkLockedThemesMethod by gettingFirstMethodDeclaratively {
    name("isLockedTheme")
    definingClass("Theme;")
}

internal val BytecodePatchContext.setThemeMethod by gettingFirstMethodDeclaratively {
    name("lambda\$updateUserBtn\$1")
    definingClass("ThemePreviewActivity;")
}
