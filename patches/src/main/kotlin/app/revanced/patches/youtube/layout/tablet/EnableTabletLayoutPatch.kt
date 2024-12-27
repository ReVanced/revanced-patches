package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.formfactor.changeFormFactorPatch

@Deprecated("Use 'Change form factor' instead.")
val enableTabletLayoutPatch = bytecodePatch {
    dependsOn(changeFormFactorPatch)
}