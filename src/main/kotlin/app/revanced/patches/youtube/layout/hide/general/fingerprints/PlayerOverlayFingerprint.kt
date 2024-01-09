package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerOverlayFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, strings = listOf("player_overlay_in_video_programming")
)
