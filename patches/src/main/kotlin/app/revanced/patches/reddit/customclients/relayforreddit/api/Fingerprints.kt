package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal fun baseClientIdFingerprint(string: String) = fingerprint {
    strings("dj-xCIZQYiLbEg", string)
}

internal val getLoggedInBearerTokenFingerprint by baseClientIdFingerprint("authorization_code")

internal val getLoggedOutBearerTokenFingerprint by baseClientIdFingerprint("https://oauth.reddit.com/grants/installed_client")

internal val getRefreshTokenFingerprint by baseClientIdFingerprint("refresh_token")

internal val loginActivityClientIdFingerprint by baseClientIdFingerprint("&duration=permanent")

internal val redditCheckDisableAPIFingerprint by fingerprint {
    opcodes(Opcode.IF_EQZ)
    strings("Reddit Disabled")
}

internal val setRemoteConfigFingerprint by fingerprint {
    parameters("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
    strings("reddit_oauth_url")
}
