package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

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

internal fun protoFieldAccessFingerprint(fieldName: String, className: String? = null) = fingerprint {
    custom { method, classDef ->
        if (className != null && !classDef.type.endsWith(className)) {
            return@custom false
        }

        val getFieldInstruction = method.indexOfFirstInstruction(Opcode.IGET_OBJECT)
        getFieldInstruction != -1 &&
                method.getInstruction(getFieldInstruction).getReference<FieldReference>()!!.name == fieldName
    }
}

internal val homeSectionFingerprint = fingerprint {
    custom { _, classDef -> classDef.endsWith("homeapi/proto/Section;") }
}

internal val homeStructureGetSectionsFingerprint =
    protoFieldAccessFingerprint("sections_", "homeapi/proto/HomeStructure;")

internal const val PENDRAGON_PROTO_FETCH_MESSAGE_LIST_RESPONSE_CLASS_NAME =
    "Lcom/spotify/pendragon/v1/proto/FetchMessageListResponse;"

internal val getMessagesFingerprint =
    protoFieldAccessFingerprint("messages_")
internal val getTriggersFingerprint =
    protoFieldAccessFingerprint("triggers_")

internal val pendragonJsonFetchMessageResponseConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    custom { _, classDef ->
        classDef.endsWith("clientmessagingplatformsdk/data/models/network/FetchMessageResponse;")
    }
}
