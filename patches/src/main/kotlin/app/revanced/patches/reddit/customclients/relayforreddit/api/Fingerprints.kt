package app.revanced.patches.reddit.customclients.relayforreddit.api

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode
import org.stringtemplate.v4.compiler.Bytecode

internal fun baseClientIdMethod(string: String) = composingFirstMethod {
    instructions(
        "dj-xCIZQYiLbEg"(),
        string(),
    )
}

internal val BytecodePatchContext.getLoggedInBearerTokenMethodMatch by baseClientIdMethod("authorization_code")

internal val BytecodePatchContext.getLoggedOutBearerTokenMethodMatch by baseClientIdMethod("https://oauth.reddit.com/grants/installed_client")

internal val BytecodePatchContext.getRefreshTokenMethodMatch by baseClientIdMethod("refresh_token")

internal val BytecodePatchContext.loginActivityClientIdMethodMatch by baseClientIdMethod("&duration=permanent")

internal val BytecodePatchContext.redditCheckDisableAPIMethod by gettingFirstMutableMethodDeclaratively("Reddit Disabled") {
    instructions(Opcode.IF_EQZ())
}

internal val BytecodePatchContext.setRemoteConfigMethod by gettingFirstMutableMethodDeclaratively("reddit_oauth_url") {
    parameterTypes("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
}
