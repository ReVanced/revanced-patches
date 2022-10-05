package app.revanced.patches.youtube.interaction.swipecontrols.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility

@Name("swipe-controls-host-activity-fingerprint")

@SwipeControlsCompatibility
@Version("0.0.1")
object SwipeControlsHostActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lapp/revanced/integrations/swipecontrols/SwipeControlsHostActivity;" && methodDef.name == "<init>"
    }
)
