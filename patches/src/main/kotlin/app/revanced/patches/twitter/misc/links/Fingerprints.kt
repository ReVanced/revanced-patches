package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val openLinkFingerprint by fingerprint {
    returns("V")
    parameters("Landroid/content/Context;", "Landroid/content/Intent;", "Landroid/os/Bundle;")
}

internal val sanitizeSharingLinksFingerprint by fingerprint {
    returns("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}

// Returns a shareable link string based on a tweet ID and a username.
internal val linkBuilderFingerprint by fingerprint {
    strings("/%1\$s/status/%2\$d")
}

// TODO remove this once changeLinkSharingDomainResourcePatch is restored
// Returns a shareable link for the "Share via..." dialog.
internal val linkResourceGetterFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/content/res/Resources;")
    custom { _, classDef ->
        classDef.fields.any { field ->
            field.type.startsWith("Lcom/twitter/model/core/")
        }
    }
}

internal val linkSharingDomainHelperFingerprint by fingerprint {
    custom { method, classDef ->
        method.name == "getShareDomain" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
