package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.shareLinkFormatterMethod by gettingFirstMutableMethodDeclaratively {
    definingClass { startsWith("Lcom/reddit/sharing/") }
    custom { immutableClassDef.sourceFile == "UrlUtil.kt" }
}
