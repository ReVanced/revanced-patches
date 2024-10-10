package app.revanced.patches.strava.upselling

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val getModulesFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { method, classDef ->
        classDef.endsWith("/GenericLayoutEntry;") && method.name == "getModules"
    }
}
