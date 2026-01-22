package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.checkLockedThemesFingerprint by gettingFirstMutableMethodDeclaratively {
    name("isLockedTheme")
    definingClass("Theme;"::endsWith)
}

internal val BytecodePatchContext.setThemeFingerprint by gettingFirstMutableMethodDeclaratively {
    name("lambda\$updateUserBtn\$1")
    definingClass("ThemePreviewActivity;"::endsWith)
}
