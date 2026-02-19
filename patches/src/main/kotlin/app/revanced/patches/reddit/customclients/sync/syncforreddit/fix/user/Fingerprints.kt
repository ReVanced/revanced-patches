package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.user

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal fun userEndpointMethodMatch(
    source: String,
    accessFlags: Set<AccessFlags>? = null,
) = composingFirstMethod {
    instructions("u/"(String::contains))
    custom { immutableClassDef.sourceFile == source }
    accessFlags(*accessFlags?.toTypedArray() ?: return@composingFirstMethod)
}

internal val BytecodePatchContext.oAuthFriendRequestMethodMatch by userEndpointMethodMatch(
    "OAuthFriendRequest.java",
)

internal val BytecodePatchContext.oAuthUnfriendRequestMethodMatch by userEndpointMethodMatch(
    "OAuthUnfriendRequest.java",
)

internal val BytecodePatchContext.oAuthUserIdRequestMethodMatch by userEndpointMethodMatch(
    "OAuthUserIdRequest.java",
)

internal val BytecodePatchContext.oAuthUserInfoRequestMethodMatch by userEndpointMethodMatch(
    "OAuthUserInfoRequest.java",
)

internal val BytecodePatchContext.oAuthSubredditInfoRequestConstructorMethodMatch by userEndpointMethodMatch(
    "OAuthSubredditInfoRequest.java",
    setOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

internal val BytecodePatchContext.oAuthSubredditInfoRequestHelperMethodMatch by userEndpointMethodMatch(
    "OAuthSubredditInfoRequest.java",
    setOf(AccessFlags.PRIVATE, AccessFlags.STATIC),
)
