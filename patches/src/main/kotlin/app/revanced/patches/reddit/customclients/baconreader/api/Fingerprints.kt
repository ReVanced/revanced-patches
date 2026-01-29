package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getAuthorizationUrlMethodMatch by composingFirstMethod {
    instructions("client_id=zACVn0dSFGdWqQ"())
}

internal val BytecodePatchContext.requestTokenMethodMatch by composingFirstMethod {
    instructions(
        "zACVn0dSFGdWqQ"(),
        "kDm2tYpu9DqyWFFyPlNcXGEni4k"(String::contains),
    )
}
