package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.fingerprint
import app.revanced.patches.youtube.layout.hide.general.yoodlesImageViewFingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val setWordmarkHeaderFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/widget/ImageView;")
    custom { methodDef, _ ->
        methodDef.containsLiteralInstruction(ytWordmarkHeaderId) &&
                methodDef.containsLiteralInstruction(ytPremiumWordmarkHeaderId)
    }
}

/**
 * Matches the same method as [yoodlesImageViewFingerprint].
 */
internal val wideSearchbarLayoutFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "L")
    literal { actionBarRingoId }
}
