package app.revanced.patches.amazonmusic.privacy

import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val preventUploadLogsPatch = bytecodePatch(
    name = "Prevent log upload",
    description = "Avoid uploading logs when the application crashes.",
) {
    compatibleWith("com.amazon.mp3")

    apply {
        pendingCrashLogsClass.firstMethod(uploadLogAfterCrashMethod).returnEarly()
        pendingCrashLogsClass.firstMethod(uploadPendingCrashLogsIfRequiredMethod).returnEarly()
    }
}
