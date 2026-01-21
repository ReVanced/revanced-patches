package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.user

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.AccessFlags

internal fun userEndpointMethodMatch(
    source: String,
    accessFlags: Set<AccessFlags>? = null
) = firstMethodComposite {
    instructions("u/"(String::contains))
    custom { immutableClassDef.sourceFile == source }
    accessFlags(*accessFlags?.toTypedArray() ?: return@firstMethodComposite)
}

internal val oAuthFriendRequestMethodMatch = userEndpointMethodMatch(
    "OAuthFriendRequest.java",
)

internal val oAuthUnfriendRequestMethodMatch = userEndpointMethodMatch(
    "OAuthUnfriendRequest.java",
)

internal val oAuthUserIdRequestMethodMatch = userEndpointMethodMatch(
    "OAuthUserIdRequest.java",
)

internal val oAuthUserInfoRequestMethodMatch = userEndpointMethodMatch(
    "OAuthUserInfoRequest.java",
)

internal val oAuthSubredditInfoRequestConstructorMethodMatch = userEndpointMethodMatch(
    "OAuthSubredditInfoRequest.java",
    setOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

internal val oAuthSubredditInfoRequestHelperMethodMatch = userEndpointMethodMatch(
    "OAuthSubredditInfoRequest.java",
    setOf(AccessFlags.PRIVATE, AccessFlags.STATIC),
)
