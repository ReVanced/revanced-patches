package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.user

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal fun userEndpointFingerprint(source: String, accessFlags: Set<AccessFlags>? = null) = fingerprint {
    strings("u/")
    custom { _, classDef -> classDef.sourceFile == source }
    accessFlags(*accessFlags?.toTypedArray() ?: return@fingerprint)
}

internal val oAuthFriendRequestFingerprint by userEndpointFingerprint(
    "OAuthFriendRequest.java",
)

internal val oAuthUnfriendRequestFingerprint by userEndpointFingerprint(
    "OAuthUnfriendRequest.java",
)

internal val oAuthUserIdRequestFingerprint by userEndpointFingerprint(
    "OAuthUserIdRequest.java",
)

internal val oAuthUserInfoRequestFingerprint by userEndpointFingerprint(
    "OAuthUserInfoRequest.java",
)

internal val oAuthSubredditInfoRequestConstructorFingerprint by userEndpointFingerprint(
    "OAuthSubredditInfoRequest.java",
    setOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

internal val oAuthSubredditInfoRequestHelperFingerprint by userEndpointFingerprint(
    "OAuthSubredditInfoRequest.java",
    setOf(AccessFlags.PRIVATE, AccessFlags.STATIC),
)
