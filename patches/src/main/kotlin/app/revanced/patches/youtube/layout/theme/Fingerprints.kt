package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.anyInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE

internal val useGradientLoadingScreenFingerprint = fingerprint {
    instructions(
        45412406L(),
    )
}

internal val splashScreenStyleFingerprint = fingerprint {
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        anyInstruction(
            1074339245(), // 20.30+
            269032877L(), // 20.29 and lower.
        ),
    )
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
}
