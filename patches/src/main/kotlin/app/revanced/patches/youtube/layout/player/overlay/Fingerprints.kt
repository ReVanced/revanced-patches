package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral

internal val createPlayerOverviewFingerprint = fingerprint {
    returns("V")
    instructions(
        resourceLiteral(ResourceType.ID, "scrim_overlay"),
        checkCast("Landroid/widget/ImageView;", location = MatchAfterWithin(10))
    )
}
