package app.revanced.patches.twitch.misc.settings

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.menuGroupsOnClickMethod by gettingFirstMutableMethodDeclaratively {
    name { contains("render") }
    definingClass { endsWith("/SettingsMenuViewDelegate;") }
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
    definingClass { endsWith("/SettingsActivity;") }
}

internal val BytecodePatchContext.settingsMenuItemEnumMethod by gettingFirstMutableMethodDeclaratively {
    name("<clinit>")
    definingClass { endsWith("/SettingsMenuItem;") }
}
