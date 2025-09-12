package app.revanced.patches.reddit.ad.general

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val adPostFingerprint by fingerprint {
    returns("V")
    // "children" are present throughout multiple versions
    strings("children")
    custom { _, classDef -> classDef.endsWith("Listing;") }
}

internal val newAdPostFingerprint by fingerprint {
    opcodes(Opcode.INVOKE_VIRTUAL)
    strings("chain", "feedElement")
    custom { _, classDef -> classDef.sourceFile == "AdElementConverter.kt" }
}
