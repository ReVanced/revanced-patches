package app.revanced.patches.instagram.misc.download

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.downloadAllowedMethodMatch by composingFirstMethod {
    instructions("clips_download_allowed_toggle_auto"())
}
