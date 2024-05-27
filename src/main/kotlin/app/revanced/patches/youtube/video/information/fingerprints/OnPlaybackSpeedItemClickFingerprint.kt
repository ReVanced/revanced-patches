package app.revanced.patches.youtube.video.information.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val onPlaybackSpeedItemClickFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L", "I", "J")
    custom { method, _ ->
        method.name == "onItemClick" && method.implementation?.instructions?.find {
            it.opcode == Opcode.IGET_OBJECT &&
                it.getReference<FieldReference>()!!.type == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;"
        } != null
    }
}
