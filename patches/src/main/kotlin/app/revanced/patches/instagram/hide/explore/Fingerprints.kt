package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.strings
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.exploreResponseJsonParserMethodMatch by composingFirstMethod("clusters") {
    strings("sectional_items")
}
