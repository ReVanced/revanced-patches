package app.revanced.patches.twitter.interaction.downloads.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val showDownloadVideoUpsellBottomSheetFingerprint = methodFingerprint {
    returns("Z")
    strings("variantToDownload.url")
    opcodes(Opcode.IF_EQZ)
}
