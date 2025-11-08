package app.revanced.patches.messenger.inbox

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal val createInboxSubTabsFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    opcodes(
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { method, classDef ->
        method.name == "run" &&
            classDef.fields.any any@{ field ->
                if (field.name != "__redex_internal_original_name") return@any false
                (field.initialValue as? StringEncodedValue)?.value == "InboxSubtabsItemSupplierImplementation\$onSubscribe\$1"
            }
    }
}

internal val loadInboxAdsFingerprint = fingerprint {
    returns("V")
    strings(
        "ads_load_begin",
        "inbox_ads_fetch_start",
    )
    custom { method, _ ->
        method.definingClass == "Lcom/facebook/messaging/business/inboxads/plugins/inboxads/itemsupplier/" +
            "InboxAdsItemSupplierImplementation;"
    }
}
