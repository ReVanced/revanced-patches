package app.revanced.patches.microsoft.officelens.misc.onedrive

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.hasMigratedToOneDriveMethod by gettingFirstMethodDeclaratively {
    name("getMigrationStage")
    definingClass("FREManager;")
}
