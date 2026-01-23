package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Fix Google login` by creatingBytecodePatch(
    description = "Allows logging in with a Google account.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    apply {
        listOf(googleOneTapAuthAvailableMethod, googleAuthAvailableMethod).forEach(MutableMethod::returnEarly)
    }
}
