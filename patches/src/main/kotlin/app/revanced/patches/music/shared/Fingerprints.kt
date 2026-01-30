package app.revanced.patches.music.shared

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal const val YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE =
    "Lcom/google/android/apps/youtube/music/activities/MusicActivity;"

internal val BytecodePatchContext.mainActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass(YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}

internal val BytecodePatchContext.conversionContextToStringMethod by gettingFirstMethodDeclaratively(
    "ConversionContext{containerInternal=",
    ", gridColumnCount=",
    ", gridColumnIndex=",
    ", templateLoggerFactory=",
    ", rootDisposableContainer=",
    ", elementId=",
    ", identifierProperty=",
) {
    name("toString")
    parameterTypes()
}
