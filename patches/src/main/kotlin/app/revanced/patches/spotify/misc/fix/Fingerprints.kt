package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint

internal val getPackageInfoFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures"
    )
}


internal val getAuthenticateResultFingerprint = fingerprint {
    strings("Unable to parse data as com.spotify.authentication.login5esperanto.EsAuthenticateResult.AuthenticateResult")
}
