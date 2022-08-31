package app.revanced.patches.tiktok.interaction.seekbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.interaction.seekbar.annotations.SeekbarCompatibility
import org.jf.dexlib2.AccessFlags

@Name("aweme-get-video-control")
@MatchingMethod("Aweme", "getVideoControl")
@SeekbarCompatibility
@Version("0.0.1")
object AwemeGetVideoControlFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC.value,
    null,
    null,
    null,
    { methodDef ->
        methodDef.definingClass.endsWith("/Aweme;") && methodDef.name == "getVideoControl"
    }
)