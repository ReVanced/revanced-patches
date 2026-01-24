package app.revanced.patches.googlenews.customtabs

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.opcodes
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val launchCustomTabMethodMatch = firstMethodComposite {
    definingClass { endsWith("CustomTabsArticleLauncher;") }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN,
    )
}
