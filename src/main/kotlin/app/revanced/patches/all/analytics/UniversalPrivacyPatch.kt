package app.revanced.patches.all.analytics

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.booleanPatchOption
import app.revanced.patches.all.analytics.google.DisableGoogleAnalytics
import app.revanced.patches.all.analytics.statsig.DisableStatsig
import java.util.logging.Logger

@Patch(
    name = "Disable privacy invasive components",
    description = "Disables multiple analytics and telemetry SDKs embedded in the app",
)
@Suppress("unused")
object UniversalPrivacyPatch : BytecodePatch(
    setOf()
) {

    // We have to create a copy of the map as a MutableMapIterator does not exist
    private val subPatchesOptionsEvaluated = mutableMapOf<Pair<String, BytecodePatch>, PatchOption<Boolean?>>()

    private val subPatchesOptions = mapOf(
        Pair("Google Analytics", DisableGoogleAnalytics) to false,
        Pair("Statsig", DisableStatsig) to true,
    ).forEach {
        subPatchesOptionsEvaluated[it.key] = booleanPatchOption(
            key = it.key.first,
            default = it.value,
            values = mapOf(
            ),
            title = "Enabled patches",
            description = "Which SDK to target",
            required = true
        )
    }


    override fun execute(context: BytecodeContext) {
        subPatchesOptionsEvaluated.forEach {
            if (it.value.value == true){
                Logger.getLogger(this::class.java.name).warning("Applying patch to disable ${it.key.first}")
                it.key.second.execute(context)
            }
        }

    }
}