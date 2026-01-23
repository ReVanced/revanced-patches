package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val createPlayerOverviewFingerprint = fingerprint {
    returnType("V")
    instructions(
        ResourceType.ID("scrim_overlay"),
        checkCast("Landroid/widget/ImageView;", location = MatchAfterWithin(10)),
    )
}
