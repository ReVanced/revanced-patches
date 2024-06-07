package app.revanced.patches.googlenews.customtabs.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object LaunchCustomTabFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN,
    ),
    customFingerprint = { _, classDef -> classDef.endsWith("CustomTabsArticleLauncher;") },
)
