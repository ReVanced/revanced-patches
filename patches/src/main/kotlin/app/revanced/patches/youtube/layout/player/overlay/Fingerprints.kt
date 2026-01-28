package app.revanced.patches.youtube.layout.player.overlay

import app.revanced.patcher.*
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.Opcode

internal val createPlayerOverviewMethodMatch = firstMethodComposite {
    returnType("V")
    instructions(
        ResourceType.ID("scrim_overlay"),
        afterAtMost(10, allOf(Opcode.CHECK_CAST(), type("Landroid/widget/ImageView;"))),
    )
}
