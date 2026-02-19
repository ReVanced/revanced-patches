package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.permalinkResponseJsonParserMethodMatch by composingFirstMethod(
    "permalink",
) {
    name("parseFromJson")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL
    )
}

internal val BytecodePatchContext.storyUrlResponseJsonParserMethodMatch by composingFirstMethod(
    "story_item_to_share_url",
) {
    name("parseFromJson")
}

internal val BytecodePatchContext.profileUrlResponseJsonParserMethodMatch by composingFirstMethod(
    "profile_to_share_url"
) {
    name("parseFromJson")
}

internal val BytecodePatchContext.liveUrlResponseJsonParserMethodMatch by composingFirstMethod(
    "live_to_share_url",
) {
    name("parseFromJson")
}
