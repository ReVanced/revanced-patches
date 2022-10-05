package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import org.jf.dexlib2.AccessFlags

@Name("remove-like-fingerprint")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.2")
object RemoveLikeFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PROTECTED or AccessFlags.CONSTRUCTOR,
    strings = listOf("like/removelike")
)