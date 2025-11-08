package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val getSpeedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;") && method.name == "onFeedSpeedSelectedEvent"
    }
}

internal val setSpeedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("Ljava/lang/String;", "Lcom/ss/android/ugc/aweme/feed/model/Aweme;", "F")
    strings("enterFrom")
}
