package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.accessFlags
import app.revanced.patcher.checkCast
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val BytecodePatchContext.createPlayerOverviewMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    instructions(
        ResourceType.ID("scrim_overlay"),
        checkCast("Landroid/widget/ImageView;", location = MatchAfterWithin(10)),
    )
}
