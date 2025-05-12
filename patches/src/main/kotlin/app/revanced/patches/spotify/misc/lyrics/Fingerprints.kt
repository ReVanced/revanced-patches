package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.fingerprint

/**
 * This method builds and returns an HTTP client for a hardcoded host (declared in this method).
 */
internal val clientBuilderFingerprint = fingerprint {
    strings("spclient.wg.spotify.com")
    parameters("Lokhttp3/OkHttpClient;", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "L", "Lio/reactivex/rxjava3/core/Scheduler;")
}