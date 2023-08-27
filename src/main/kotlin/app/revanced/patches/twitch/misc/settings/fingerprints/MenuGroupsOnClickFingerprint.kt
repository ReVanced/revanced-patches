package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object MenuGroupsOnClickFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PRIVATE or AccessFlags.STATIC or AccessFlags.FINAL,
    listOf("L", "L", "L"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsMenuViewDelegate;")
                && methodDef.name.contains("render")
    }
)
