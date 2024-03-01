package app.revanced.patches.ticktick.misc.premium.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object UserDataIsProFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("User;") && methodDef.name == "isPro"
    }
)
