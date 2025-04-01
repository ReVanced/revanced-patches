package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val accountAttributeFingerprint = fingerprint {
    custom { _, c -> c.endsWith("internal/AccountAttribute;") }
}

internal val productStateProtoFingerprint = fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef ->
        classDef.endsWith("ProductStateProto;")
    }
}

internal val buildQueryParametersFingerprint = fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuExperimentsFingerprint = fingerprint {
    parameters("L")
    strings("remove_ads_upsell_enabled")
}

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