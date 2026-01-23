package app.revanced.patches.twitch.misc.settings

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.menuGroupsOnClickMethod by gettingFirstMutableMethodDeclaratively {
    name("render"::contains)
    definingClass("/SettingsMenuViewDelegate;"::endsWith)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "L", "L")
}

internal val BytecodePatchContext.menuGroupsUpdatedMethod by gettingFirstMutableMethodDeclaratively {
    name("<init>")
    definingClass("/SettingsMenuPresenter\$Event\$MenuGroupsUpdated;")
}

internal val BytecodePatchContext.settingsActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass("/SettingsActivity;"::endsWith)
}

internal val BytecodePatchContext.settingsMenuItemEnumMethod by gettingFirstMutableMethodDeclaratively {
    name("<clinit>")
    definingClass("/SettingsMenuItem;"::endsWith)
}
