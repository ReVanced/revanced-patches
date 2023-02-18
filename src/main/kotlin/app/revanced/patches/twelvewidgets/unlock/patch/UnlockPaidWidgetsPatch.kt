package app.revanced.patches.twelvewidgets.unlock.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twelvewidgets.unlock.fingerprints.*

@Patch
@Name("unlock-paid-widgets")
@Description("Unlocks paid widgets of the app")
@Compatibility([Package("com.dci.dev.androidtwelvewidgets")])
@Version("0.0.1")
class UnlockPaidWidgetsPatch : BytecodePatch(
    listOf(
        AgendaDaysWidgetUnlockFingerprint,
        CalendarBigWidgetUnlockFingerprint,
        CalendarWideDayEventsWidgetUnlockFingerprint,
        CalendarWideTimelineWidgetUnlockFingerprint,
        ScreentimeSmallWidgetUnlockFingerprint,
        WeatherWidgetUnlockFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        listOf(
            AgendaDaysWidgetUnlockFingerprint,
            CalendarBigWidgetUnlockFingerprint,
            CalendarWideDayEventsWidgetUnlockFingerprint,
            CalendarWideTimelineWidgetUnlockFingerprint,
            ScreentimeSmallWidgetUnlockFingerprint,
            WeatherWidgetUnlockFingerprint
        ).map { fingerprint ->
            fingerprint.result?.mutableMethod ?: return fingerprint.toErrorResult()
        }.forEach { method ->
            method.apply {
                removeInstructions(4, 2)
                addInstructions(
                    implementation?.instructions?.size!!, """
                    const/4 v1, 0x0
                    invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                    return-object v0
                """
                )
            }
        }

        return PatchResultSuccess()
    }
}
