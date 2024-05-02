package app.revanced.patches.strava.upselling.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val getModulesFingerprint = methodFingerprint {
    opcodes(Opcode.IGET_OBJECT)
    custom { methodDef, classDef ->
        classDef.type.endsWith("/GenericLayoutEntry;") && methodDef.name == "getModules"
    }
}
