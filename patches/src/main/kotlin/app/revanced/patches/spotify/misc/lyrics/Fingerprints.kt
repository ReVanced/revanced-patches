package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

/**
 * This method builds and returns an HTTP client for a hardcoded host (declared in this method).
 */
internal val clientBuilderFingerprint = fingerprint {
    strings("spclient.wg.spotify.com")
    parameters("Lokhttp3/OkHttpClient;", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "L", "Lio/reactivex/rxjava3/core/Scheduler;")
}

/**
 * This method is where the HTTP client for lyrics is defined.
 * This patch will replace this HTTP client with a patched HTTP client for the required custom lyrics host.
 */
context(BytecodePatchContext)
internal val executeFingerprint: Fingerprint
    get() = fingerprint {
        returns(clientBuilderFingerprint.originalMethod.returnType)
        parameters()
        opcodes(Opcode.CHECK_CAST)
    }