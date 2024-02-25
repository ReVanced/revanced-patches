package app.revanced.patches.twitter.interaction.downloads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object BuildMediaOptionsSheetFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO_16,
        Opcode.NEW_INSTANCE,
    ),
    strings = listOf("resources.getString(R.string.post_video)"),
)
