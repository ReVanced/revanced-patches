package app.revanced.patches.tumblr.annoyances.inappupdate

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.featureflags.OverrideFeatureFlagsPatch

@Patch(
    name = "Disable in-app update",
    description = "Disables the in-app update check and update prompt.",
    dependencies = [OverrideFeatureFlagsPatch::class],
    compatiblePackages = [CompatiblePackage("com.tumblr")],
)
@Suppress("unused")
object DisableInAppUpdatePatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {
        // Before checking for updates using Google Play core AppUpdateManager, the value of this feature flag is checked.
        // If this flag is false or the last update check was today and no update check is performed.
        OverrideFeatureFlagsPatch.addOverride("inAppUpdate", "false")
    }
}
