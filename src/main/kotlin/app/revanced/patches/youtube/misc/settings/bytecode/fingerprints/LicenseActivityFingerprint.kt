package app.revanced.patches.youtube.misc.settings.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility

// TODO: This is more of a class fingerprint than a method fingerprint.
//  Convert to a class fingerprint whenever possible.
@Name("license-activity-fingerprint")
@MatchingMethod(
    "Lcom/google/android/libraries/social/licenses/LicenseActivity;", "onCreate"
)
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
object LicenseActivityFingerprint : MethodFingerprint(
    null,
    null,
    null,
    null,
    null,
    { methodDef ->
        methodDef.definingClass.endsWith("LicenseActivity;") && methodDef.name == "onCreate"
    }
)