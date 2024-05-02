package app.revanced.patches.magazines.misc.gms.customtabs.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.extensions.or
import com.android.tools.smali.dexlib2.AccessFlags



internal object UseCustomTabsFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsArticleLauncher;" && methodDef.accessFlags == AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR
    },
    opcodes = listOf(
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN
    ),
)
