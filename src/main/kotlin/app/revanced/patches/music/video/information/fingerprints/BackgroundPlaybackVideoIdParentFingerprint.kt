package app.revanced.patches.music.video.information.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object BackgroundPlaybackVideoIdParentFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = emptyList(),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("currentWatchNextResponse")
)
