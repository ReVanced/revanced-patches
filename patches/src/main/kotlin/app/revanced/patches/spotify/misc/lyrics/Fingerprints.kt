package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val httpClientBuilderFingerprint = fingerprint {
    strings("client == null", "scheduler == null")
}

internal fun getLyricsHttpClientFingerprint(httpClientBuilderMethodReference: MethodReference) =
    fingerprint {
        returns(httpClientBuilderMethodReference.returnType)
        parameters()
        custom { method, _ ->
            method.indexOfFirstInstruction {
                getReference<MethodReference>() == httpClientBuilderMethodReference
            } >= 0
        }
    }
