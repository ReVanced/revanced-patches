package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getLoggedInBearerTokenMethod by gettingFirstMutableMethodDeclaratively(
    "dj-xCIZQYiLbEg", "authorization_code"
)

internal val BytecodePatchContext.getLoggedOutBearerTokenMethod by gettingFirstMutableMethodDeclaratively(
    "dj-xCIZQYiLbEg", "https://oauth.reddit.com/grants/installed_client"
)

internal val BytecodePatchContext.getRefreshTokenMethod by gettingFirstMutableMethodDeclaratively(
    "dj-xCIZQYiLbEg", "refresh_token"
)

internal val BytecodePatchContext.loginActivityClientIdMethod by gettingFirstMutableMethodDeclaratively(
    "dj-xCIZQYiLbEg", "&duration=permanent"
)

internal val BytecodePatchContext.redditCheckDisableAPIMethod by gettingFirstMutableMethodDeclaratively("Reddit Disabled") {
    instructions(Opcode.IF_EQZ())
}

internal val BytecodePatchContext.setRemoteConfigMethod by gettingFirstMutableMethodDeclaratively("reddit_oauth_url") {
    parameterTypes("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
}
