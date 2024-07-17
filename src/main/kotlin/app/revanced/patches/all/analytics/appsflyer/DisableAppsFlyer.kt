package app.revanced.patches.all.analytics.appsflyer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.analytics.appsflyer.fingerprints.AppsFlyerInitFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Disable AppsFlyer analytics SDK",
    use = false,
)
@Suppress("unused")
object DisableAppsFlyer : BytecodePatch(
    setOf(AppsFlyerInitFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AppsFlyerInitFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                """
                    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                    return-object p0
                    """
        )
    }
}