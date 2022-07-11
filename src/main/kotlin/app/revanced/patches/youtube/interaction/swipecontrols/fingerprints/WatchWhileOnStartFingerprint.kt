package app.revanced.patches.youtube.interaction.swipecontrols.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility

@Name("watch-while-onStart-fingerprint")
@MatchingMethod(
    "LWatchWhileActivity;", "onCreate"
)
@DirectPatternScanMethod
@SwipeControlsCompatibility
@Version("0.0.1")
object WatchWhileOnStartFingerprint : MethodFingerprint(
    null, null, null, null, null, { methodDef ->
        methodDef.definingClass.endsWith("WatchWhileActivity;") && methodDef.name == "onStart"
    }
)