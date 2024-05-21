package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val mainActivityOnBackPressedFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { methodDef, _ ->
        val matchesClass = methodDef.definingClass.endsWith("MainActivity;") ||
            // Old versions of YouTube called this class "WatchWhileActivity" instead.
            methodDef.definingClass.endsWith("WatchWhileActivity;")

        matchesClass && methodDef.name == "onBackPressed"
    }
}
