package app.revanced.patches.messenger.ads.inbox.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object LoadInboxAdsFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf(
        "ads_load_begin",
        "inbox_ads_fetch_start"
    ),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/facebook/messaging/business/inboxads/plugins/inboxads/itemsupplier/InboxAdsItemSupplierImplementation;"
    }
)

