package app.revanced.patches.shared.misc.checks

import app.revanced.patcher.gettingFirstMutableClassDefDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.patchInfoClassDef by gettingFirstMutableClassDefDeclaratively(
    "Lapp/revanced/extension/shared/checks/PatchInfo;"
)

internal val BytecodePatchContext.patchInfoBuildClassDef by gettingFirstMutableClassDefDeclaratively(
    $$"Lapp/revanced/extension/shared/checks/PatchInfo$Build;"
)
