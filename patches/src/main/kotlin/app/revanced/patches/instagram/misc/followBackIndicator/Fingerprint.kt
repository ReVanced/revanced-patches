package app.revanced.patches.instagram.misc.followBackIndicator
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal const val INTERNAL_BADGE_TARGET_STRING = "bindInternalBadges"
internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/instagram/misc/followbackindicator/"
internal const val EXTENSION_HELPER_CLASS_DESCRIPTOR = "${EXTENSION_CLASS_DESCRIPTOR}Helper;"

internal val bindInternalBadgeFingerprint = fingerprint {
    strings(INTERNAL_BADGE_TARGET_STRING)
}

internal val bindRowViewTypesFingerprint = fingerprint {
    strings("NONE should not map to item type")
}

internal val nametagResultCardViewSetButtonMethodFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    custom { method, classDef ->
        classDef.type.endsWith("NametagResultCardView;") && method.parameters.size == 3
    }
}

internal val getFollowbackInfoExtensionFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getFollowbackInfo" && classDef.type == EXTENSION_HELPER_CLASS_DESCRIPTOR
    }
}

internal val getViewingProfileUserObjectExtensionFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getViewingProfileUserObject" && classDef.type == EXTENSION_HELPER_CLASS_DESCRIPTOR
    }
}
