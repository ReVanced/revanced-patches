package app.revanced.patches.youtube.general.tablet

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.general.formfactor.changeFormFactorPatch

@Deprecated("Use 'Change form factor' instead.")
val enableTabletLayoutPatch = bytecodePatch {
    dependsOn(changeFormFactorPatch)
}