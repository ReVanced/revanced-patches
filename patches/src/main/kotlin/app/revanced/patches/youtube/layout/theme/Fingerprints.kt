package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.anyInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE

internal val useGradientLoadingScreenFingerprint = fingerprint {
    instructions(
        literal(45412406L)
    )
}

internal val splashScreenStyleFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    instructions(
        anyInstruction(
            literal(1074339245), // 20.30+
            literal(269032877L) // 20.29 and lower.
        )
    )
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
}
