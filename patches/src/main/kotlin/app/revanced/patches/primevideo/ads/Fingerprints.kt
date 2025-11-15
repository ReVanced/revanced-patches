package app.revanced.patches.primevideo.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val enterServerInsertedAdBreakStateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    parameters("Lcom/amazon/avod/fsm/Trigger;")
    returns("V")
    custom { method, classDef ->
        method.name == "enter" && classDef.type == "Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;"
    }
}

internal val doTriggerFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED)
    returns("V")
    custom { method, classDef ->
        method.name == "doTrigger" && classDef.type == "Lcom/amazon/avod/fsm/StateBase;"
    }
}

internal val onSeekPastUnwatchedAdFingerprint = fingerprint {
    custom {method, classDef ->
        method.name == "onSeekPastUnwatchedAd" && classDef.endsWith("SeekbarControllerImpl;")
    }
}

internal val onSeekBehindUnwatchedAdFingerprint = fingerprint {
    custom {method, classDef ->
        method.name == "onSeekBehindUnwatchedAd" && classDef.endsWith("SeekbarControllerImpl;")
    }
}