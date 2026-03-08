package app.revanced.patches.photoshopmix

import app.revanced.patcher.fingerprint

internal val disableLoginFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("CreativeCloudSource;") && method.name == "isLoggedIn"
    }
    returns("Z")
}

internal val libButtonClickedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("PSMixFragment;") && method.name == "ccLibButtonClickHandler"
    }
}

internal val lightroomButtonClickedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("PSMixFragment;") && method.name == "lightroomButtonClickHandler"
    }
}

internal val ccButtonClickedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("PSMixFragment;") && method.name == "ccButtonClickHandler"
    }
}