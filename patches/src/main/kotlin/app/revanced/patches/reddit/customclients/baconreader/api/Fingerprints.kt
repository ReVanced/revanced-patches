package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke

internal val getAuthorizationUrlMethodMatch = firstMethodComposite {
    instructions("client_id=zACVn0dSFGdWqQ"())
}

internal val requestTokenMethodMatch = firstMethodComposite {
    instructions(
        "zACVn0dSFGdWqQ"(),
        "kDm2tYpu9DqyWFFyPlNcXGEni4k"(String::contains)
    )
}