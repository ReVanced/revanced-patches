package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility

@Name("text-component-spec-parent-fingerprint")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
object TextComponentSpecParentFingerprint : MethodFingerprint(
    strings = listOf("TextComponentSpec: No converter for extension: ")
)