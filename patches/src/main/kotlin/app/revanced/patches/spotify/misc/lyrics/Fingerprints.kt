package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.httpClientBuilderMethod by gettingFirstMethodDeclaratively {
    strings("client == null", "scheduler == null")
}

internal fun getLyricsHttpClientFingerprint(httpClientBuilderMethodReference: MethodReference) = fingerprint {
    returnType(httpClientBuilderMethodReference.returnType)
    parameterTypes()
    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<MethodReference>() == httpClientBuilderMethodReference
        } >= 0
    }
}
