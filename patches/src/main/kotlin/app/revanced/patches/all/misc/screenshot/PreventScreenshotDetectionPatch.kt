package app.revanced.patches.all.misc.screenshot

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.filterImplementationsOf
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.Method

@Suppress("unused")
val preventScreenshotDetectionPatch = bytecodePatch(
    name = "Prevent screenshot detection",
    description = "Prevents the app from detecting screenshots.",
) {
    execute {
        fun Method.isOnScreenCaptured() =
            returnType == "V" && name == "onScreenCaptured" && parameterTypes.isEmpty() && implementation != null

        classes.filterImplementationsOf("Landroid/app/Activity\$ScreenCaptureCallback;")
            .filter { it.virtualMethods.any { method -> method.isOnScreenCaptured() } }.map { proxy(it).mutableClass }
            .forEach { it.virtualMethods.first { method -> method.isOnScreenCaptured() }.returnEarly() }
    }
}
