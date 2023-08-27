package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object SpeedControlParentFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    parameters = listOf(
        "L"
    ),
    strings = listOf(
        "playback_speed"
    )
)