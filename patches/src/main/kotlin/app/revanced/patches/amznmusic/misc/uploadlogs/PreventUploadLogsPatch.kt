package app.revanced.patches.amznmusic.misc.uploadlogs

import app.revanced.patcher.firstClassDef
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
        var pendingCrashLogsClass = firstClassDef { type == "Lcom/amazon/mp3/det/PendingCrashLogs;" }
        pendingCrashLogsClass.firstMethod { name == "uploadLogAfterCrash" }.returnEarly()
        pendingCrashLogsClass.firstMethod { name == "uploadPendingCrashLogsIfRequired" }.returnEarly()
    }
}
