package app.revanced.patches.instagram.hide.highlightsTray

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal const val TARGET_STRING = "highlights_tray"

internal val BytecodePatchContext.highlightsUrlBuilderMethodMatch by composingFirstMethod("X-IG-Accept-Hint") {
    instructions(TARGET_STRING())
}
