package app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object RedditCheckDisableAPIFingerprint : MethodFingerprint(
    strings = listOf("Reddit Disabled"),
    opcodes = listOf(Opcode.IF_EQZ)
)