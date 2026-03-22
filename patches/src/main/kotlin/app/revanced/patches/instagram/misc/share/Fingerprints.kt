package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.permalinkResponseJsonParserMethodMatch by composingFirstMethod {
    instructions("permalink"())
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL
    )
}

internal val BytecodePatchContext.storyUrlResponseJsonParserMethodMatch by composingFirstMethod {
    instructions("story_item_to_share_url"())
}

internal val BytecodePatchContext.profileUrlResponseJsonParserMethodMatch by composingFirstMethod {
    instructions("profile_to_share_url"())
}

internal val BytecodePatchContext.liveUrlResponseJsonParserMethodMatch by composingFirstMethod {
    instructions("live_to_share_url"())
}
