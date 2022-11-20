package app.revanced.patches.youtube.ad.general.bytecode.patch

import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.ad.general.annotation.GeneralAdsCompatibility
import app.revanced.patches.youtube.misc.settings.framework.components.impl.*

@DependsOn()
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralAdsBytecodePatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        TODO("Not yet implemented")
    }

}
