package app.revanced.patches.twitter.interaction.downloads

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val buildMediaOptionsSheetFingerprint = fingerprint {
    opcodes(
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.NEW_INSTANCE,
    )
    strings("resources.getString(R.string.post_video)")
}

internal val constructMediaOptionsSheetFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    strings("captionsState")
}

internal val showDownloadVideoUpsellBottomSheetFingerprint = fingerprint {
    returns("Z")
    strings("variantToDownload.url")
    opcodes(Opcode.IF_EQZ)
}
