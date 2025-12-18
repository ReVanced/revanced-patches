package app.revanced.patches.reddit.ad.general

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val adPostFingerprint = fingerprint {
    returns("V")
    // "children" are present throughout multiple versions
    strings("children")
    custom { _, classDef -> classDef.endsWith("Listing;") }
}

internal val newAdPostFingerprint = fingerprint {
    opcodes(Opcode.INVOKE_VIRTUAL)
    strings("feedElement", "com.reddit.cookie")
}
