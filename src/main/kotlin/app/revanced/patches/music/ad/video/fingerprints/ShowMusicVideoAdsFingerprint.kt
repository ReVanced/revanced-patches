package app.revanced.patches.music.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.ad.video.annotations.MusicVideoAdsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("show-video-ads-method-fingerprint")
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@MusicVideoAdsCompatibility
@Version("0.0.1")
object ShowMusicVideoAdsFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("Z"), listOf(
        Opcode.IPUT_BOOLEAN,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)
