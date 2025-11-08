package app.revanced.patches.twitch.misc.settings

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val menuGroupsOnClickFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "L")
    custom { method, classDef ->
        classDef.endsWith("/SettingsMenuViewDelegate;") &&
            method.name.contains("render")
    }
}

internal val menuGroupsUpdatedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SettingsMenuPresenter\$Event\$MenuGroupsUpdated;") &&
            method.name == "<init>"
    }
}

internal val settingsActivityOnCreateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SettingsActivity;") &&
            method.name == "onCreate"
    }
}

internal val settingsMenuItemEnumFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SettingsMenuItem;") && method.name == "<clinit>"
    }
}
