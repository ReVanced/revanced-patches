package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.checkLockedThemesFingerprint by gettingFirstMutableMethodDeclaratively {
    name("isLockedTheme")
    definingClass("Theme;"::endsWith)
}

internal val BytecodePatchContext.setThemeMethod by gettingFirstMutableMethodDeclaratively {
    name("lambda\$updateUserBtn\$1")
    definingClass("ThemePreviewActivity;"::endsWith)
}
