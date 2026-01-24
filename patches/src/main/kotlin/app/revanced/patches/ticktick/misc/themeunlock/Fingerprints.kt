package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.checkLockedThemesFingerprint by gettingFirstMutableMethodDeclaratively {
    name("isLockedTheme")
    definingClass { endsWith("Theme;") }
}

internal val BytecodePatchContext.setThemeMethod by gettingFirstMutableMethodDeclaratively {
    name("lambda\$updateUserBtn\$1")
    definingClass { endsWith("ThemePreviewActivity;") }
}
