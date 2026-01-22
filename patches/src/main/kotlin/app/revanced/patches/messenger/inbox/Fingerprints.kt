package app.revanced.patches.messenger.inbox

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal val BytecodePatchContext.createInboxSubTabsMethod by gettingFirstMutableMethodDeclaratively {
    name("run")
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    opcodes(
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom {
        immutableClassDef.fields.any { field ->
            if (field.name != "__redex_internal_original_name") return@any false
            (field.initialValue as? StringEncodedValue)?.value == "InboxSubtabsItemSupplierImplementation\$onSubscribe\$1"
        }
    }
}

internal val BytecodePatchContext.loadInboxAdsMethod by gettingFirstMutableMethodDeclaratively(
    "ads_load_begin",
    "inbox_ads_fetch_start"
) {
    returnType("V")
    definingClass(
        "Lcom/facebook/messaging/business/inboxads/plugins/inboxads/itemsupplier/" +
                "InboxAdsItemSupplierImplementation;"
    )
}
