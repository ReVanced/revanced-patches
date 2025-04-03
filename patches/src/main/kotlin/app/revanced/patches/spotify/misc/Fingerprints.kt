package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val accountAttributeFingerprint = fingerprint {
    custom { _, c -> c.endsWith("internal/AccountAttribute;") }
}

internal val productStateProtoFingerprint = fingerprint {
    returns("Ljava/util/Map;")
    custom { _, c -> c.endsWith("ProductStateProto;") }
}

internal val buildQueryParametersFingerprint = fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuExperimentsFingerprint = fingerprint {
    parameters("L")
    strings("remove_ads_upsell_enabled")
}

internal val homeSectionFingerprint = fingerprint {
    custom { _, c -> c.endsWith("homeapi/proto/Section;") }
}

internal val protobufListsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    custom { m, _ -> m.name == "emptyProtobufList" }
}

internal val homeStructureFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT, Opcode.RETURN_OBJECT)
    custom { _, c -> c.endsWith("homeapi/proto/HomeStructure;") }
}
