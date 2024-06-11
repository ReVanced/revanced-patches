package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val miniplayerModernConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    literal { 45623000L }
}
