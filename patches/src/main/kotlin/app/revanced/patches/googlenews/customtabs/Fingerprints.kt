package app.revanced.patches.googlenews.customtabs

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.launchCustomTabMethodMatch by composingFirstMethod {
    definingClass("CustomTabsArticleLauncher;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN,
    )
}
