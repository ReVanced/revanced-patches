package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.buildMediaOptionsSheetMethodMatch by composingFirstMethod("mediaEntity", "media_options_sheet") {
    opcodes(
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.NEW_INSTANCE,
    )
}

internal val BytecodePatchContext.constructMediaOptionsSheetMethodMatch by composingFirstMethod("captionsState") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.showDownloadVideoUpsellBottomSheetMethodMatch by composingFirstMethod("mediaEntity", "url") {
    returnType("Z")
    opcodes(Opcode.IF_EQZ)
}
