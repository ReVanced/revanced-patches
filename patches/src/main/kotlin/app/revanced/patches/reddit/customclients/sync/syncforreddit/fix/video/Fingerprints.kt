package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.video

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.Opcode

internal val parseRedditVideoNetworkResponseMethodMatch = firstMethodComposite {
    name("parseNetworkResponse")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.CONST_WIDE_32,
    )
    custom { immutableClassDef.sourceFile == "RedditVideoRequest.java" }
}
