package app.revanced.patches.youtube.interaction.swipecontrols.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility

@Name("watch-while-activity-fingerprint")

@SwipeControlsCompatibility
@Version("0.0.1")
object WatchWhileActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("WatchWhileActivity;") && methodDef.name == "<init>"
    }
)
