package app.revanced.patches.youtube.flyoutpanel.player.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object PlaybackLoopOnClickListenerFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("I", "Z"),
    strings = listOf("menu_item_single_video_playback_loop")
)