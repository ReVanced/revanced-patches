package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MenuGroupsOnClickFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PRIVATE or AccessFlags.STATIC or AccessFlags.FINAL,
    listOf("L", "L", "L"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsMenuViewDelegate;")
                && methodDef.name.contains("render")
    }
)
