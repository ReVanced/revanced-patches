package app.revanced.patches.tiktok.interaction.speed

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val getSpeedFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;") && methodDef.name == "onFeedSpeedSelectedEvent"
    }
}

internal val onRenderFirstFrameFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;") && methodDef.name == "onRenderFirstFrame"
    }
}

internal val setSpeedFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("Ljava/lang/String;", "Lcom/ss/android/ugc/aweme/feed/model/Aweme;", "F")
    strings("enterFrom")
}
