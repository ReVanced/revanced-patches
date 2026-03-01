package app.revanced.patches.tiktok.interaction.cleardisplay

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val onClearDisplayEventFingerprint = fingerprint {
    custom { method, classDef ->
        // Internally the feature is called "Clear mode".
        classDef.endsWith("/ClearModePanelComponent;") && method.name == "onClearModeEvent"
    }
}

internal val clearModeLogCoreFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Z",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
        "Ljava/lang/String;",
        "J",
        "I"
    )
}

internal val clearModeLogStateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("V")
    parameters(
        "Lcom/bytedance/common/utility/collection/WeakHandler;",
        "Z",  
        "Ljava/lang/String;",
        "Lcom/ss/android/ugc/aweme/feed/model/Aweme;", 
        "J"   
        "I",     
        "I"       
    )
}

internal val clearModeLogPlaytimeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters(
        "F",
        "I",
        "J",
        "J",
        "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Z" 
    )
}