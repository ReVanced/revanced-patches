package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val clientBuilderFingerprint = fingerprint {
    strings("spclient.wg.spotify.com")
    parameters("Lokhttp3/OkHttpClient;", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "L", "Lio/reactivex/rxjava3/core/Scheduler;")
}

internal val oauthHostnameCheckFingerprint = fingerprint {
    strings("spclient.wg.spotify.com")
    parameters("Lokhttp3/Request;")
    returns("Z")
}

internal val webgateHostnameCheckFingerprint = fingerprint {
    strings("spclient.wg.spotify.com", "Authorization")
}

context(BytecodePatchContext)
internal val executeFingerprint: Fingerprint
    get() = fingerprint {
        returns(clientBuilderFingerprint.originalMethod.returnType)
        parameters()
        opcodes(Opcode.CHECK_CAST)
    }