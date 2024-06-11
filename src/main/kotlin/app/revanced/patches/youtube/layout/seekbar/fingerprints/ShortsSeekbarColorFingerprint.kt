package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.seekbar.reelTimeBarPlayedColorId
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val shortsSeekbarColorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    literal { reelTimeBarPlayedColorId }
}
