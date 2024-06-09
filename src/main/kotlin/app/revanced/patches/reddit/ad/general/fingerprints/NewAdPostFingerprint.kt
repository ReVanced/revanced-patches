package app.revanced.patches.reddit.ad.general.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val newAdPostFingerprint = methodFingerprint {
    opcodes(Opcode.INVOKE_VIRTUAL)
    strings("chain", "feedElement")
    custom { _, classDef -> classDef.sourceFile == "AdElementConverter.kt"}
}