package app.revanced.patches.twitter.interaction.downloads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val buildMediaOptionsSheetFingerprint = fingerprint {
    opcodes(
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.NEW_INSTANCE,
    )
    strings("mediaEntity", "media_options_sheet")
}

internal val constructMediaOptionsSheetFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    strings("captionsState")
}

internal val showDownloadVideoUpsellBottomSheetFingerprint = fingerprint {
    returns("Z")
    strings("mediaEntity", "url")
    opcodes(Opcode.IF_EQZ)
}
