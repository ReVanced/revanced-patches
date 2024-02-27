package app.revanced.patches.youtube.fullscreen.forcefullscreen.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ClientSettingEndpointFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "Ljava/util/Map;"),
    strings = listOf(
        "OVERRIDE_EXIT_FULLSCREEN_TO_MAXIMIZED",
        "force_fullscreen",
        "start_watch_minimized",
        "watch"
    )
)