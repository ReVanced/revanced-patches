package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.sanitizeSharingLinksMethod by gettingFirstMethodDeclaratively {
    returnType("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}

// Returns a shareable link string based on a tweet ID and a username.
internal val BytecodePatchContext.linkBuilderMethod by gettingFirstMethodDeclaratively {
    strings("/%1\$s/status/%2\$d")
}

// TODO remove this once changeLinkSharingDomainResourcePatch is restored
// Returns a shareable link for the "Share via..." dialog.
internal val BytecodePatchContext.linkResourceGetterMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Landroid/content/res/Resources;")
    custom { _, classDef ->
        classDef.fields.any { field ->
            field.type.startsWith("Lcom/twitter/model/core/")
        }
    }
}

internal val BytecodePatchContext.linkSharingDomainHelperMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        method.name == "getShareDomain" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
