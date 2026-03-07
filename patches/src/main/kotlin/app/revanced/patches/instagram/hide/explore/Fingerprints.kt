package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.exploreResponseJsonParserMethodMatch by composingFirstMethod("clusters") {
    name("unsafeParseFromJson")
    instructions("sectional_items"())
}
