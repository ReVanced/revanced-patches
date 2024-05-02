package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val mainActivityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("MainActivity;") ||
            // Old versions of YouTube called this class "WatchWhileActivity" instead.
            methodDef.definingClass.endsWith("WatchWhileActivity;")
    }
}
