package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val menuGroupsUpdatedFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsMenuPresenter\$Event\$MenuGroupsUpdated;") &&
            methodDef.name == "<init>"
    }
}
