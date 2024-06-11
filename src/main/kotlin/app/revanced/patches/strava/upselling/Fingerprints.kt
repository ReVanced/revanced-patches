package app.revanced.patches.strava.upselling

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint.methodFingerprint

internal val getModulesFingerprint = methodFingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { methodDef, classDef ->
        classDef.endsWith("/GenericLayoutEntry;") && methodDef.name == "getModules"
    }
}
