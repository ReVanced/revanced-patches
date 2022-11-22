package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object SpeedControlParentFingerprint : MethodFingerprint(
    returnType = "L",
    access = AccessFlags.PRIVATE or AccessFlags.FINAL,
    parameters = listOf(
        "L"
    ),
    strings = listOf(
        "playback_speed"
    )
)