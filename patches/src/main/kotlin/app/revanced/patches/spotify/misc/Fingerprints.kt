package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

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

internal val abstractProtobufListEnsureIsMutableFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    returns("V")
    custom { method, _ -> method.indexOfFirstInstruction {
            getReference<TypeReference>()?.type == "Ljava/lang/UnsupportedOperationException;"
        } >= 0
    }
}

internal val homeSectionFingerprint = fingerprint {
    custom { _, classDef -> classDef.endsWith("homeapi/proto/Section;") }
}

internal val homeStructureGetSectionsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("homeapi/proto/HomeStructure;") && method.indexOfFirstInstruction {
            opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.name == "sections_"
        } >= 0
    }
}

internal fun reactivexFunctionApplyWithClassInitFingerprint(className: String) = fingerprint {
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    custom { method, _ -> method.name == "apply" && method.indexOfFirstInstruction {
            opcode == Opcode.NEW_INSTANCE && getReference<TypeReference>()?.type?.endsWith(className) == true
        } >= 0
    }
}

internal const val PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME = "FetchMessageRequest;"
internal val pendragonJsonFetchMessageRequestFingerprint =
    reactivexFunctionApplyWithClassInitFingerprint(PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME)

internal const val PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME = "FetchMessageListRequest;"
internal val pendragonProtoFetchMessageListRequestFingerprint =
    reactivexFunctionApplyWithClassInitFingerprint(PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME)
