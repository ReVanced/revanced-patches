package app.revanced.patches.shared.misc.checks

import app.revanced.patcher.gettingFirstClassDefDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.patchInfoClassDef by gettingFirstClassDefDeclaratively(
    "Lapp/revanced/extension/shared/checks/PatchInfo;"
)

internal val BytecodePatchContext.patchInfoBuildClassDef by gettingFirstClassDefDeclaratively(
    $$"Lapp/revanced/extension/shared/checks/PatchInfo$Build;"
)
