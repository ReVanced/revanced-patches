package app.revanced.patches.music.layout.startpage

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.coldStartUpMethodMatch by composingFirstMethod {
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        "FEmusic_library_sideloaded_tracks"(),
        "FEmusic_home"()
    )
}
