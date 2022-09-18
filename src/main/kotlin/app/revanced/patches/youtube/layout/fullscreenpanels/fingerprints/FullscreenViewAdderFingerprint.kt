package app.revanced.patches.youtube.layout.fullscreenpanels.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.pivotbar.shortsbutton.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.Opcode

@Name("fullscreen-view-adder-fingerprint")
@MatchingMethod(
    "LFullscreenEngagementPanelOverlay;", "e"
)
@DirectPatternScanMethod
@ShortsButtonCompatibility
@Version("0.0.1")
object FullscreenViewAdderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_BOOLEAN
    )
)
