package app.revanced.patches.youtube.layout.amoled.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.PatchDeprecated
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.layout.amoled.annotations.AmoledCompatibility
import app.revanced.patches.youtube.layout.customthemes.patch.CustomThemePatch
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch

@Patch
@DependsOn([FixLocaleConfigErrorPatch::class])
@Name("amoled")
@Description("Enables pure black theme.")
@AmoledCompatibility
@Version("0.0.1")
@PatchDeprecated("Theme patch already includes the Amoled theme.", CustomThemePatch::class)
class AmoledPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        CustomThemePatch.themeOption.value = CustomThemePatch.Themes.AMOLED.name
        return CustomThemePatch().execute(data)
    }
}
