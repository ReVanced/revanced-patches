package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val accountAttributeFingerprint by fingerprint {
    custom { _, classDef -> classDef.endsWith("internal/AccountAttribute;") }
}

internal val productStateProtoFingerprint by fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef -> classDef.endsWith("ProductStateProto;") }
}

internal val buildQueryParametersFingerprint by fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuExperimentsFingerprint by fingerprint {
    parameters("L")
    strings("remove_ads_upsell_enabled")
}

internal val homeSectionFingerprint by fingerprint {
    custom { _, classDef -> classDef.endsWith("homeapi/proto/Section;") }
}

internal val protobufListsFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    custom { method, _ -> method.name == "emptyProtobufList" }
}

internal val homeStructureFingerprint by fingerprint {
    opcodes(Opcode.IGET_OBJECT, Opcode.RETURN_OBJECT)
    custom { _, classDef -> classDef.endsWith("homeapi/proto/HomeStructure;") }
}
