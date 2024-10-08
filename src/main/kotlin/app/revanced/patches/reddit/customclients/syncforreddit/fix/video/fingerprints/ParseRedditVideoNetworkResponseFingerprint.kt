package app.revanced.patches.reddit.customclients.syncforreddit.fix.video.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ParseRedditVideoNetworkResponseFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.NEW_INSTANCE,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.CONST_WIDE_32
    ),
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "RedditVideoRequest.java" && methodDef.name == "parseNetworkResponse"
    }
)
