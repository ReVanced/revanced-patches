package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val openLinkFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/content/Context;", "Landroid/content/Intent;", "Landroid/os/Bundle;")
}

internal val sanitizeSharingLinksFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}

// Returns a shareable link string based on a tweet ID and a username.
internal val linkBuilderFingerprint = fingerprint {
    strings("/%1\$s/status/%2\$d")
}

// TODO remove this once changeLinkSharingDomainResourcePatch is restored
// Returns a shareable link for the "Share via..." dialog.
internal val linkResourceGetterFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/content/res/Resources;")
    custom { _, classDef ->
        classDef.fields.any { field ->
            field.type.startsWith("Lcom/twitter/model/core/")
        }
    }
}

internal val linkSharingDomainHelperFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getShareDomain" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
