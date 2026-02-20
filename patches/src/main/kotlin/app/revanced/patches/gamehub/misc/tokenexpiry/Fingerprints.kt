package app.revanced.patches.gamehub.misc.tokenexpiry

import app.revanced.patcher.fingerprint

internal val routerUtilsTokenExpiryFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/router/RouterUtils;" && method.name == "n"
    }
}

internal val routerUtilsGuideLoginFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/router/RouterUtils;" && method.name == "z"
    }
}

// Targets invokeSuspend inside the $checkGuideStep$1 coroutine continuation.
// We inject a goto that skips the guide-step validation and jumps directly to the
// DeviceManager readiness check, replicating the reference diff's goto/16 :goto_3 approach.
internal val routerUtilsGuideStepFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/router/RouterUtils${'$'}checkGuideStep${'$'}1;" &&
            method.name == "invokeSuspend"
    }
}
