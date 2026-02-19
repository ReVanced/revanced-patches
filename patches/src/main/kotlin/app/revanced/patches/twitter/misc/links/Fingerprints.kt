package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.sanitizeSharingLinksMethod by gettingFirstMethod(
    "<this>",
    "shareParam",
    "sessionToken",
) { returnType == "Ljava/lang/String;" }

// Returns a shareable link string based on a tweet ID and a username.
internal val BytecodePatchContext.linkBuilderMethod by gettingFirstMethod($$"/%1$s/status/%2$d")

// TODO remove this once changeLinkSharingDomainResourcePatch is restored
// Returns a shareable link for the "Share via..." dialog.
internal val BytecodePatchContext.linkResourceGetterMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Landroid/content/res/Resources;")
    custom {
        immutableClassDef.anyField { type.startsWith("Lcom/twitter/model/core/") }
    }
}

internal val BytecodePatchContext.linkSharingDomainHelperMethod by gettingFirstMethodDeclaratively {
    name("getShareDomain")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
}
