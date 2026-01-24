package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstMutableMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.string

internal val getAuthorizationStringMethodMatch = firstMethodComposite {
    instructions(string { startsWith("authorize.compact?client_id") })
}

internal val getBearerTokenMethodMatch = firstMethodComposite {
    instructions(string { startsWith("Basic") })
}

internal val BytecodePatchContext.getUserAgentMethod by gettingFirstMutableMethod(
    "android:com.laurencedawson.reddit_sync",
)

internal val imgurImageAPIMethodMatch = firstMethodComposite {
    instructions("https://imgur-apiv3.p.rapidapi.com/3/image"())
}
