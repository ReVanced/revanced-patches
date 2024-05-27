package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val mainActivityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { _, classDef ->
        // Old versions of YouTube called this class "WatchWhileActivity" instead.
        classDef.endsWith("MainActivity;") || classDef.endsWith("WatchWhileActivity;")
    }
}
