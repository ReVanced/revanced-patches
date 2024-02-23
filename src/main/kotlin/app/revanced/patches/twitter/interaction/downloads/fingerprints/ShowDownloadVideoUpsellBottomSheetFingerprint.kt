package app.revanced.patches.twitter.interaction.downloads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ShowDownloadVideoUpsellBottomSheetFingerprint : MethodFingerprint(
    returnType = "Z",
    strings = listOf("variantToDownload.url"),
    opcodes = listOf(Opcode.IF_EQZ)
)
