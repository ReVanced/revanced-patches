package app.revanced.patches.youtube.misc.recyclerviewtree.hook

import app.revanced.patcher.accessFlags
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.opcodes
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val recyclerViewTreeObserverMethodMatch = firstMethodComposite("LithoRVSLCBinder") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.NEW_INSTANCE,
    )
}
