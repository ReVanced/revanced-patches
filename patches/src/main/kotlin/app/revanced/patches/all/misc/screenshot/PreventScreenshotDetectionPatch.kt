package app.revanced.patches.all.misc.screenshot

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val preventScreenshotDetectionPatch = bytecodePatch(
    name = "Prevent screenshot detection",
    description = "Prevents the app from detecting screenshots.",
) {
    execute {
        classes.filter { it.interfaces.contains("Landroid/app/Activity\$ScreenCaptureCallback;") }
            .map { proxy(it).mutableClass.virtualMethods }.forEach { methods ->
                methods.first { it.name == "onScreenCaptured" }.returnEarly()
            }
    }
}
