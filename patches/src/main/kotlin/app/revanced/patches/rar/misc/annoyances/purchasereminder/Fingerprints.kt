package app.revanced.patches.rar.misc.annoyances.purchasereminder

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val showReminderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    custom { method, _ ->
        method.definingClass.endsWith("AdsNotify;") && method.name == "show"
    }
}
