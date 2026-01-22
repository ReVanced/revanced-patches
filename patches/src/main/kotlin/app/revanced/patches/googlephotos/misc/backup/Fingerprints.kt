package app.revanced.patches.googlephotos.misc.backup

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isDCIMFolderBackupControlMethod by gettingFirstMutableMethodDeclaratively("/dcim", "/mars_files/") {
    returnType("Z")
}
