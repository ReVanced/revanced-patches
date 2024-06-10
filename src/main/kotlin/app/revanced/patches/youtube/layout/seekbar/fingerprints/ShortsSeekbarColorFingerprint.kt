package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.seekbar.reelTimeBarPlayedColorId
import com.android.tools.smali.dexlib2.AccessFlags

internal val shortsSeekbarColorFingerprint = methodFingerprint(
    literal { reelTimeBarPlayedColorId },
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}
