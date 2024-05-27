package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patches.youtube.layout.seekbar.reelTimeBarPlayedColorId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val shortsSeekbarColorFingerprint = literalValueFingerprint(
    literalSupplier = { reelTimeBarPlayedColorId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}
