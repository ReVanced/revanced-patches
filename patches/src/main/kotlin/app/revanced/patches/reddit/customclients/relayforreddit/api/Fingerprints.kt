package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode


internal fun baseClientIdMethod(string: String) = firstMethodComposite {
    instructions(
        "dj-xCIZQYiLbEg"(),
        string()
    )
}

internal val getLoggedInBearerTokenMethodMatch = baseClientIdMethod("authorization_code")

internal val getLoggedOutBearerTokenMethodMatch = baseClientIdMethod("https://oauth.reddit.com/grants/installed_client")

internal val getRefreshTokenMethodMatch = baseClientIdMethod("refresh_token")

internal val loginActivityClientIdMethodMatch = baseClientIdMethod("&duration=permanent")

internal val BytecodePatchContext.redditCheckDisableAPIMethod by gettingFirstMutableMethodDeclaratively("Reddit Disabled") {
    instructions(Opcode.IF_EQZ())
}

internal val BytecodePatchContext.setRemoteConfigMethod by gettingFirstMutableMethodDeclaratively("reddit_oauth_url") {
    parameterTypes("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
}
