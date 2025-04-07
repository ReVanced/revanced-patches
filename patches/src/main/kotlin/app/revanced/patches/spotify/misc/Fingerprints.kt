package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val SPOTIFY_ACCOUNT_ATTRIBUTE = "Lcom/spotify/remoteconfig/internal/AccountAttribute;"

/**
 * Version 8.6.98.900.
 */
private const val SPOTIFY_ACCOUNT_ATTRIBUTE_LEGACY = "Lcom/spotify/useraccount/v1/AccountAttribute;"

internal val accountAttributeFingerprint = fingerprint {
    custom { _, classDef ->
        classDef.type == if (SPOTIFY_LEGACY_APP_TARGET) {
            SPOTIFY_ACCOUNT_ATTRIBUTE_LEGACY
        } else {
            SPOTIFY_ACCOUNT_ATTRIBUTE
        }
    }
}

internal val productStateProtoFingerprint = fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef ->
        classDef.type == if (SPOTIFY_LEGACY_APP_TARGET) {
            "Lcom/spotify/ucs/proto/v0/UcsResponseWrapper${'$'}AccountAttributesResponse;"
        } else {
            "Lcom/spotify/remoteconfig/internal/ProductStateProto;"
        }
    }
}

internal val buildQueryParametersFingerprint = fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuExperimentsFingerprint = fingerprint {
    parameters("L")
    strings("remove_ads_upsell_enabled")
}

internal val homeSectionFingerprint = fingerprint {
    custom { _, classDef -> classDef.endsWith("homeapi/proto/Section;") }
}

internal val protobufListsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    custom { method, _ -> method.name == "emptyProtobufList" }
}

internal val homeStructureFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT, Opcode.RETURN_OBJECT)
    custom { _, classDef -> classDef.endsWith("homeapi/proto/HomeStructure;") }
}
