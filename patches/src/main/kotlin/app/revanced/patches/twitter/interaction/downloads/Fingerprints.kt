package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.buildMediaOptionsSheetMethod by gettingFirstMethodDeclaratively {
    opcodes(
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.NEW_INSTANCE,
    )
    strings("mediaEntity", "media_options_sheet")
}

internal val BytecodePatchContext.constructMediaOptionsSheetMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("captionsState")
}

internal val BytecodePatchContext.showDownloadVideoUpsellBottomSheetMethod by gettingFirstMethodDeclaratively {
    returnType("Z")
    strings("mediaEntity", "url")
    opcodes(Opcode.IF_EQZ)
}
