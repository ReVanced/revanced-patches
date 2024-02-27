package app.revanced.patches.youtube.player.hapticfeedback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object SeekUndoHapticsFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("Failed to execute seek undo haptics vibrate.")
)
