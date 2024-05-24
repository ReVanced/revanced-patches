package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal object CreatePlayerRequestBodyWithModelFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation!!.instructions.any {
            it.getReference<FieldReference>().toString() == "Landroid/os/Build;->MODEL:Ljava/lang/String;"
        }
    },
)
