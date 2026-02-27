package app.revanced.patches.music.misc.fileprovider

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.fileProviderResolverMethod by gettingFirstMethodDeclaratively(
    "android.support.FILE_PROVIDER_PATHS",
    "Name must not be empty"
) {
    returnType("L")
}