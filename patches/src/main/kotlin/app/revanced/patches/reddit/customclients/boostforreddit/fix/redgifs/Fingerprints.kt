package app.revanced.patches.reddit.customclients.boostforreddit.fix.redgifs

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.classDef
import app.revanced.patcher.custom
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method

internal val BytecodePatchContext.createOkHttpClientMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE)
    instructions(
        Opcode.NEW_INSTANCE(),
        Opcode.INVOKE_DIRECT(),
        Opcode.NEW_INSTANCE(),
        Opcode.INVOKE_DIRECT(),
        Opcode.NEW_INSTANCE(),
        Opcode.INVOKE_DIRECT(),
        Opcode.INVOKE_VIRTUAL(),
        Opcode.MOVE_RESULT_OBJECT()
    )

    // Helper to capture the BytecodePatchContext for classDef access
    fun Method.isTargetSourceFile() = classDef.sourceFile == "RedGifsAPIv2.java"
    custom { isTargetSourceFile() }
}
