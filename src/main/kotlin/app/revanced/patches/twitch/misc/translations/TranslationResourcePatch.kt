package app.revanced.patches.twitch.misc.translations

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitch.misc.strings.StringsPatch

@Patch(
    //name = "Translations", // TODO: Once the patch is ready, uncomment this line.
    description = "Adds translations to ReVanced patches.",
    dependencies = [StringsPatch::class],
    compatiblePackages = [
        CompatiblePackage("tv.twitch.android.app")
    ]
)
@Suppress("unused")
object TranslationResourcePatch : ResourcePatch() {
    private const val TRANSLATION_RESOURCE_DIRECTORY = "twitch/translation"

    override fun execute(context: ResourceContext) {
        // Look in the jar file and find the paths of the translation string files.
        StringsPatch.copyLocalizedStringFiles(TRANSLATION_RESOURCE_DIRECTORY)
    }

}