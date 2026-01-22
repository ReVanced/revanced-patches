package app.revanced.patches.music.misc.fileprovider

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.fileProviderResolverMethod by gettingFirstMutableMethodDeclaratively(
    "android.support.FILE_PROVIDER_PATHS",
    "Name must not be empty"
) {
    returnType("L")
}