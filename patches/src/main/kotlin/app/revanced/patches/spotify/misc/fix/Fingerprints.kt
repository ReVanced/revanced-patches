package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val getPackageInfoFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures"
    )
}

internal val katanaProxyLoginMethodHandlerClassFingerprint = fingerprint {
    strings("katana_proxy_auth")
}

internal val katanaProxyLoginMethodTryAuthorizeFingerprint = fingerprint {
    strings("e2e")
}
