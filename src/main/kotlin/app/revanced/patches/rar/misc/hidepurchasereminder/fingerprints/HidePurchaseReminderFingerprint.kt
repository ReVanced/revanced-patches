package app.revanced.patches.rar.misc.hidepurchasereminder.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object HidePurchaseReminderFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("AdsNotify;") && methodDef.name == "show"
    }
)