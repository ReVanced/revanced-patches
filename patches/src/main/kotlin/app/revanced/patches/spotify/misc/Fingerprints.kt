package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

context(BytecodePatchContext)
internal val accountAttributeFingerprint get() = fingerprint {
    custom { _, classDef -> classDef.type == "Lcom/spotify/remoteconfig/internal/AccountAttribute;" }
}

context(BytecodePatchContext)
internal val productStateProtoGetMapFingerprint get() = fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef -> classDef.type == "Lcom/spotify/remoteconfig/internal/ProductStateProto;" }
}

internal val buildQueryParametersFingerprint by fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuViewModelClassFingerprint by fingerprint {
    strings("ContextMenuViewModel(header=")
}

/**
 * Used in versions older than "9.0.60.128".
 */
internal val oldContextMenuViewModelAddItemFingerprint by fingerprint {
    parameters("L")
    returns("V")
    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<MethodReference>()?.name == "add"
        } >= 0
    }
}

internal val contextMenuViewModelConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

/**
 * Used to find the interface name of a context menu item.
 */
internal val removeAdsContextMenuItemClassFingerprint by fingerprint {
    strings("remove_ads_item", "ui_navigate")
}

internal const val CONTEXT_MENU_ITEM_CLASS_DESCRIPTOR_PLACEHOLDER = "Lapp/revanced/ContextMenuItemPlaceholder;"
internal val extensionFilterContextMenuItemsFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/util/List;")
    parameters("Ljava/util/List;")
    custom { method, classDef ->
        method.name == "filterContextMenuItems" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val getViewModelFingerprint by fingerprint {
    custom { method, _ -> method.name == "getViewModel" }
}

internal val contextFromJsonFingerprint by fingerprint {
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC
    )
    custom { method, classDef ->
        method.name == "fromJson" &&
                classDef.type.endsWith("voiceassistants/playermodels/ContextJsonAdapter;")
    }
}

internal val readPlayerOptionOverridesFingerprint by fingerprint {
    custom { method, classDef ->
        method.name == "readPlayerOptionOverrides" &&
                classDef.type.endsWith("voiceassistants/playermodels/PreparePlayOptionsJsonAdapter;")
    }
}

internal val protobufListsFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    custom { method, _ -> method.name == "emptyProtobufList" }
}

internal val abstractProtobufListEnsureIsMutableFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    returns("V")
    custom { method, _ ->
    	method.indexOfFirstInstruction {
            getReference<TypeReference>()?.type == "Ljava/lang/UnsupportedOperationException;"
        } >= 0
    }
}

internal fun structureGetSectionsFingerprint(className: String) = fingerprint {
    custom { method, classDef ->
        classDef.type.endsWith(className) && method.indexOfFirstInstruction {
            opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.name == "sections_"
        } >= 0
    }
}

internal val homeSectionFingerprint by fingerprint {
    custom { _, classDef -> classDef.type.endsWith("homeapi/proto/Section;") }
}

internal val homeStructureGetSectionsFingerprint by
    structureGetSectionsFingerprint("homeapi/proto/HomeStructure;")

internal val browseSectionFingerprint by fingerprint {
    custom { _, classDef-> classDef.type.endsWith("browsita/v1/resolved/Section;") }
}

internal val browseStructureGetSectionsFingerprint by
    structureGetSectionsFingerprint("browsita/v1/resolved/BrowseStructure;")

internal fun reactivexFunctionApplyWithClassInitFingerprint(className: String) = fingerprint {
    returns("Ljava/lang/Object;")
    parameters("Ljava/lang/Object;")
    custom { method, _ ->
        method.name == "apply" && method.indexOfFirstInstruction {
            opcode == Opcode.NEW_INSTANCE && getReference<TypeReference>()?.type?.endsWith(className) == true
        } >= 0
    }
}

internal const val PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME = "FetchMessageRequest;"
internal val pendragonJsonFetchMessageRequestFingerprint by
    reactivexFunctionApplyWithClassInitFingerprint(PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME)

internal const val PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME = "FetchMessageListRequest;"
internal val pendragonProtoFetchMessageListRequestFingerprint by
    reactivexFunctionApplyWithClassInitFingerprint(PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME)
