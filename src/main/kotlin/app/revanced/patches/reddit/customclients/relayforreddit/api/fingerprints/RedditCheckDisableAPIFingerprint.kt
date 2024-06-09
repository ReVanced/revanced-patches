package app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val redditCheckDisableAPIFingerprint = methodFingerprint {
    opcodes(Opcode.IF_EQZ)
    strings("Reddit Disabled")
}