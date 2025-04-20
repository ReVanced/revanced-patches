package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.layout.hide.general.yoodlesImageViewFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val setWordmarkHeaderFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/widget/ImageView;")
    instructions(
        resourceLiteral("attr", "ytPremiumWordmarkHeader"),
        resourceLiteral("attr", "ytWordmarkHeader")
    )
}

/**
 * Matches the same method as [yoodlesImageViewFingerprint].
 */
internal val wideSearchbarLayoutFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "L")
    instructions(
         resourceLiteral("layout", "action_bar_ringo"),
    )
}
