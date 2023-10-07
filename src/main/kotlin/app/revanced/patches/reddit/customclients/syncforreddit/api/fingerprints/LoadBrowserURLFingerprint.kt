package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object LoadBrowserURLFingerprint : MethodFingerprint(
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
    opcodes = listOf(Opcode.CONST_4),
    strings = listOf("CustomInterface")
)