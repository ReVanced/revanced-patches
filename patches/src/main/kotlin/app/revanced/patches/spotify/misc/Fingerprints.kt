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

internal val buildQueryParametersFingerprint = fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuViewModelClassFingerprint = fingerprint {
    strings("ContextMenuViewModel(header=")
}

/**
 * Used in versions older than "9.0.60.128".
 */
internal val oldContextMenuViewModelAddItemFingerprint = fingerprint {
    parameters("L")
    returns("V")
    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<MethodReference>()?.name == "add"
        } >= 0
    }
}

internal val contextMenuViewModelConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

/**
 * Used to find the interface name of a context menu item.
 */
internal val removeAdsContextMenuItemClassFingerprint = fingerprint {
    strings("remove_ads_item", "ui_navigate")
}

internal const val CONTEXT_MENU_ITEM_CLASS_DESCRIPTOR_PLACEHOLDER = "Lapp/revanced/ContextMenuItemPlaceholder;"
internal val extensionFilterContextMenuItemsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/util/List;")
    parameters("Ljava/util/List;")
    custom { method, classDef ->
        method.name == "filterContextMenuItems" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val getViewModelFingerprint = fingerprint {
    custom { method, _ -> method.name == "getViewModel" }
}

internal val contextFromJsonFingerprint = fingerprint {
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

internal val readPlayerOptionOverridesFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "readPlayerOptionOverrides" &&
                classDef.type.endsWith("voiceassistants/playermodels/PreparePlayOptionsJsonAdapter;")
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

internal val homeSectionFingerprint = fingerprint {
    custom { _, classDef -> classDef.type.endsWith("homeapi/proto/Section;") }
}

internal val homeStructureGetSectionsFingerprint =
    structureGetSectionsFingerprint("homeapi/proto/HomeStructure;")

internal val browseSectionFingerprint = fingerprint {
    custom { _, classDef-> classDef.type.endsWith("browsita/v1/resolved/Section;") }
}

internal val browseStructureGetSectionsFingerprint =
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
internal val pendragonJsonFetchMessageRequestFingerprint =
    reactivexFunctionApplyWithClassInitFingerprint(PENDRAGON_JSON_FETCH_MESSAGE_REQUEST_CLASS_NAME)

internal const val PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME = "FetchMessageListRequest;"
internal val pendragonProtoFetchMessageListRequestFingerprint =
    reactivexFunctionApplyWithClassInitFingerprint(PENDRAGON_PROTO_FETCH_MESSAGE_LIST_REQUEST_CLASS_NAME)
