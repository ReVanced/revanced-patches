package app.revanced.patches.youtube.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility
import org.jf.dexlib2.Opcode

@Name("load-ads-fingerprint")

@VideoAdsCompatibility
@Version("0.0.1")
object LoadAdsFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.INVOKE_INTERFACE_RANGE),
    customFingerprint = { method ->
        method.parameterTypes.size > 0 && method.parameterTypes.first().endsWith("InstreamAdBreak;")
    }
)