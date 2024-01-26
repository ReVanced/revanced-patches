package app.revanced.patches.youtube.video.information.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal object OnPlaybackSpeedItemClickFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("L", "L", "I", "J"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "onItemClick" && methodDef.implementation?.instructions?.find {
            it.opcode == Opcode.IGET_OBJECT &&
                    it.getReference<FieldReference>()!!.type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"
        } != null
    }
)