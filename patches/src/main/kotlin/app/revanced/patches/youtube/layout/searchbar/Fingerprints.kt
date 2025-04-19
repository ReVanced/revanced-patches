package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val setWordmarkHeaderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/widget/ImageView;")
    custom { methodDef, _ ->
        methodDef.containsLiteralInstruction(ytWordmarkHeaderId) &&
                methodDef.containsLiteralInstruction(ytPremiumWordmarkHeaderId)
    }
}
