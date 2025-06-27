package app.revanced.patches.cricbuzz.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val userStateSwitchFingerprint by fingerprint {
    strings("key.user.state", "NA")
    opcodes(Opcode.SPARSE_SWITCH)
}
