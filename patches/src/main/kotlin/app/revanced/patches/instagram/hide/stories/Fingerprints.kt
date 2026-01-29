package app.revanced.patches.instagram.hide.stories

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getOrCreateAvatarViewMethodMatch by composingFirstMethod {
    definingClass("Lcom/instagram/reels/ui/views/reelavatar/RecyclerReelAvatarView;")
    parameterTypes()
    returnType("L")
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_VIRTUAL, // Add View (Story).
    )
}
