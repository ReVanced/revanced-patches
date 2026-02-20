package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getSpeedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;") && method.name == "onFeedSpeedSelectedEvent"
    }
}

internal val speedOptionEnabledFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters("Lcom/ss/android/ugc/aweme/feed/model/Aweme;")
    custom { method, classDef ->
        classDef.type == "LX/0MbX;" && method.name == "LIZ"
    }
}
