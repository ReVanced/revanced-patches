package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.fingerprint

// This method builds and returns an HTTP client with a hardcoded host (declared in this method).
internal val httpClientBuilderFingerprint = fingerprint {
    strings("client == null", "scheduler == null")
}
