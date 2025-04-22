package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val accountAttributeFingerprint = fingerprint {
    custom { _, classDef ->
        classDef.type == if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            "Lcom/spotify/useraccount/v1/AccountAttribute;"
        } else {
            "Lcom/spotify/remoteconfig/internal/AccountAttribute;"
        }
    }
}

internal val productStateProtoGetMapFingerprint = fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef ->
        classDef.type == if (IS_SPOTIFY_LEGACY_APP_TARGET) {
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

internal val contextFromJsonFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
    )
    custom { methodDef, classDef ->
        methodDef.name == "fromJson" &&
                classDef.endsWith("voiceassistants/playermodels/ContextJsonAdapter;")
    }
}

internal val readPlayerOptionOverridesFingerprint = fingerprint {
    custom { methodDef, classDef ->
        methodDef.name == "readPlayerOptionOverrides" &&
                classDef.endsWith("voiceassistants/playermodels/PreparePlayOptionsJsonAdapter;")
    }
}

internal val protobufListsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    custom { method, _ -> method.name == "emptyProtobufList" }
}

internal val homeSectionFingerprint = fingerprint {
    custom { _, classDef -> classDef.endsWith("homeapi/proto/Section;") }
}

internal val homeStructureGetSectionsFingerprint = fingerprint {
    opcodes(Opcode.IGET_OBJECT, Opcode.RETURN_OBJECT)
    custom { _, classDef -> classDef.endsWith("homeapi/proto/HomeStructure;") }
}

internal const val PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME = "FetchMessageRequest;"
internal val pendragonJsonFetchMessageRequest = fingerprint {
    strings("nowplayingview:npv-open-mv:v1")
    custom { method, _ -> method.name == "apply" }
}

internal const val PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME = "FetchMessageListRequest;"
internal val pendragonProtoFetchMessageListRequest = fingerprint {
    strings("Canvas disabled")
    custom { method, _ -> method.name == "apply" }
}
