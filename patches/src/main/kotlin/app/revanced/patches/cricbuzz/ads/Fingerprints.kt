package app.revanced.patches.cricbuzz.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val userStateSwitchFingerprint = fingerprint {
    strings("key.user.state", "NA")
    opcodes(Opcode.SPARSE_SWITCH)
}

internal val    cb11ConstructorFingerprint = fingerprint {
    parameters("Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;", "I", "Ljava/lang/String;", "Ljava/lang/String;", "Z", "L")
    custom { _, classDef -> classDef.endsWith("CB11Details;") }
}

internal val getBottomBarFingerprint = fingerprint {
    custom { method, classDef -> method.name == "getBottomBar" && classDef.endsWith("HomeMenu;") }
}