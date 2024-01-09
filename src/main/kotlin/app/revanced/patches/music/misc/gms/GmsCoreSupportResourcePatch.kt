package app.revanced.patches.music.misc.gms

import app.revanced.patcher.data.ResourceContext
import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.strings.StringsPatch
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportResourcePatch

object GmsCoreSupportResourcePatch : AbstractGmsCoreSupportResourcePatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    spoofedPackageSignature = "afb0fed5eeaebdd86f56a97742f4b6b33ef59875",
    dependencies = setOf(
        StringsPatch::class
    )
) {
    override fun execute(context: ResourceContext) {
        // Strings used by different target apps.
        StringsPatch.includeSharedPatchStrings("GmsCoreSupport")

        super.execute(context)
    }
}