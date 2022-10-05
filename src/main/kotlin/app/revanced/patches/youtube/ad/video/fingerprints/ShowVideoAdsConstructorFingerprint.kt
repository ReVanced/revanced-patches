package app.revanced.patches.youtube.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("show-video-ads-constructor-fingerprint")
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@VideoAdsCompatibility
@Version("0.0.1")
object ShowVideoAdsConstructorFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR, listOf("L", "L", "L"), listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        null, // either CONST_4 or CONST_16
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN,
        Opcode.RETURN_VOID
    )
)