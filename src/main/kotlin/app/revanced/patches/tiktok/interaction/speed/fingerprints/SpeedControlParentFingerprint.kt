package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.interaction.speed.annotations.SpeedCompatibility
import org.jf.dexlib2.AccessFlags

@Name("speed-control-parent-fingerprint")
@MatchingMethod("LX/4cP;", "LJIILL")
@SpeedCompatibility
@Version("0.0.1")
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