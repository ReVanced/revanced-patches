package app.revanced.patches.spotify.misc.voicesearch

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val contextFromJsonFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
        )
    custom { methodDef, classDef ->
        classDef.endsWith("voiceassistants/playermodels/ContextJsonAdapter;") && methodDef.name == "fromJson"
    }
}

internal val readPlayerOptionOverridesFingerprint = fingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("voiceassistants/playermodels/PreparePlayOptionsJsonAdapter;") && methodDef.name == "readPlayerOptionOverrides"
    }
}