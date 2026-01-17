package app.revanced.patches.reddit.customclients.boostforreddit.fix.redgifs

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

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
    custom { immutableClassDef.sourceFile == "RedGifsAPIv2.java" }
}
