package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val menuGroupsOnClickFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "L")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsMenuViewDelegate;") &&
            methodDef.name.contains("render")
    }
}
