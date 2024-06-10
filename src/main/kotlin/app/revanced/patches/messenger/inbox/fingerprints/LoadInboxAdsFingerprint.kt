package app.revanced.patches.messenger.inbox.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val loadInboxAdsFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    strings(
        "ads_load_begin",
        "inbox_ads_fetch_start"
    )
    custom { _, classDef ->
        classDef.type == "Lcom/facebook/messaging/business/inboxads/plugins/inboxads/itemsupplier/InboxAdsItemSupplierImplementation;"
    }
}
