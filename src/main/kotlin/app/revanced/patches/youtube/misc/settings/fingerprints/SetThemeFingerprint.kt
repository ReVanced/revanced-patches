package app.revanced.patches.youtube.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.misc.settings.appearanceStringId
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val setThemeFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { appearanceStringId }
}
