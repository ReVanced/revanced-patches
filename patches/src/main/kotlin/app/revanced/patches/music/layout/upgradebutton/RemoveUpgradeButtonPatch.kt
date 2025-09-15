package app.revanced.patches.music.layout.upgradebutton

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Patch was renamed", ReplaceWith("hideUpgradeButton"))
@Suppress("unused")
val removeUpgradeButton = bytecodePatch{
    dependsOn(hideUpgradeButton)
}
