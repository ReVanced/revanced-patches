package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getAuthorizationStringMethod by gettingFirstMutableMethodDeclaratively("authorize.compact?client_id")

internal val BytecodePatchContext.getBearerTokenMethod by gettingFirstMutableMethodDeclaratively("Basic")

internal val BytecodePatchContext.getUserAgentMethod by gettingFirstMutableMethodDeclaratively("android:com.laurencedawson.reddit_sync")

internal val BytecodePatchContext.imgurImageAPIMethod by gettingFirstMutableMethodDeclaratively("https://imgur-apiv3.p.rapidapi.com/3/image")
