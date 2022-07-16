package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import org.jf.dexlib2.AccessFlags

@Name("remove-like-fingerprint")
@MatchingMethod(
    "Luqw;", "<init>"
)
@FuzzyPatternScanMethod(2)
@ReturnYouTubeDislikeCompatibility
@Version("0.0.2")
object RemoveLikeFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PROTECTED or AccessFlags.CONSTRUCTOR,
    null,
    null,
    listOf("like/removelike")
)