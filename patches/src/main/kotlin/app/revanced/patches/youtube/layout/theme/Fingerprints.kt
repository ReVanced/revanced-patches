package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.accessFlags
import app.revanced.patcher.anyInstruction
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE

internal val BytecodePatchContext.useGradientLoadingScreenMethod by gettingFirstMethodDeclaratively {
    instructions(
        45412406L(),
    )
}

internal val BytecodePatchContext.splashScreenStyleMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    instructions(
        anyInstruction(
            1074339245L(), // 20.30+
            269032877L(), // 20.29 and lower.
        ),
    )
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
}
