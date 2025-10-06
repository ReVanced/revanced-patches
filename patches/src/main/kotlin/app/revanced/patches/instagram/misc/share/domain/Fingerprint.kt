package app.revanced.patches.instagram.misc.share.domain

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/misc/share/domain/ChangeLinkSharingDomainPatch;"

internal val getCustomShareDomainFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { method, classDef ->
        method.name == "getCustomShareDomain" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
