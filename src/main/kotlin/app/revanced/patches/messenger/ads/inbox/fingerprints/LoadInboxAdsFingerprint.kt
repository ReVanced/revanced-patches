package app.revanced.patches.messenger.ads.inbox.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object LoadInboxAdsFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf(
        "ads_load_begin",
        "inbox_ads_fetch_start"
    ),
    access = AccessFlags.PUBLIC or AccessFlags.STATIC,
    customFingerprint = {
        it.definingClass == "Lcom/facebook/messaging/business/inboxads/plugins/inboxads/itemsupplier/InboxAdsItemSupplierImplementation;"
    }
)

