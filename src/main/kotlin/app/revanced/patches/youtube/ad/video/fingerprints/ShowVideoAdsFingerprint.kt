package app.revanced.patches.youtube.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("show-video-ads-method-fingerprint")

@VideoAdsCompatibility
@Version("0.0.1")
object ShowVideoAdsFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("Z")
)