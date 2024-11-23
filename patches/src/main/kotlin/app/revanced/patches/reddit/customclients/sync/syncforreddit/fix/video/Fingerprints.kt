package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.video

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val parseRedditVideoNetworkResponseFingerprint = fingerprint {
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.CONST_WIDE_32,
    )
    custom { methodDef, classDef ->
        classDef.sourceFile == "RedditVideoRequest.java" && methodDef.name == "parseNetworkResponse"
    }
}
