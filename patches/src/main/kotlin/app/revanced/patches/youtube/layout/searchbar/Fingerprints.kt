package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.layout.hide.general.yoodlesImageViewFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val setWordmarkHeaderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/widget/ImageView;")
    instructions(
        resourceLiteral(ResourceType.ATTR, "ytPremiumWordmarkHeader"),
        resourceLiteral(ResourceType.ATTR, "ytWordmarkHeader")
    )
}

/**
 * Matches the same method as [yoodlesImageViewFingerprint].
 */
internal val wideSearchbarLayoutFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "L")
    instructions(
         resourceLiteral(ResourceType.LAYOUT, "action_bar_ringo"),
    )
}
