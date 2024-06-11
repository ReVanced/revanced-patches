package app.revanced.patches.twitch.misc.settings

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val menuGroupsOnClickFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "L")
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingsMenuViewDelegate;") &&
            methodDef.name.contains("render")
    }
}

internal val menuGroupsUpdatedFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingsMenuPresenter\$Event\$MenuGroupsUpdated;") &&
            methodDef.name == "<init>"
    }
}

internal val settingsActivityOnCreateFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingsActivity;") &&
            methodDef.name == "onCreate"
    }
}

internal val settingsMenuItemEnumFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingsMenuItem;") && methodDef.name == "<clinit>"
    }
}
