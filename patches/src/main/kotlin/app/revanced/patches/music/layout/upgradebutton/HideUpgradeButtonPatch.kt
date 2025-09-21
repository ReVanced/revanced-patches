package app.revanced.patches.music.layout.upgradebutton

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.layout.navigationbar.navigationBarPatch

@Deprecated("Patch is obsolete and was replaced by navigation bar patch", ReplaceWith("navigationBarPatch"))
@Suppress("unused")
val hideUpgradeButton = bytecodePatch{
    dependsOn(navigationBarPatch)
}

@Deprecated("Patch was renamed", ReplaceWith("hideUpgradeButton"))
@Suppress("unused")
val removeUpgradeButton = bytecodePatch{
    dependsOn(hideUpgradeButton)
}
