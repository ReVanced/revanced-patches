package app.revanced.patches.youtube.misc.autorepeat.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object AutoRepeatFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf(),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation!!.instructions.count() == 3 && methodDef.annotations.isEmpty()
    }
)