package app.revanced.patches.reddit.customclients.sync.syncforreddit.annoyances.startup

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.mainActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("MainActivity;") }
}
