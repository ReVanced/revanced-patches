package app.revanced.patches.amazonmusic.privacy

import app.revanced.patcher.gettingFirstClassDefDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.name
import app.revanced.patcher.type

internal val BytecodePatchContext.pendingCrashLogsClass by gettingFirstClassDefDeclaratively {
    type("Lcom/amazon/mp3/det/PendingCrashLogs;")
}

internal val BytecodePatchContext.uploadLogAfterCrashMethod by gettingFirstMethodDeclaratively {
    name("uploadLogAfterCrash")
}

internal val BytecodePatchContext.uploadPendingCrashLogsIfRequiredMethod by gettingFirstMethodDeclaratively {
    name("uploadPendingCrashLogsIfRequired")
}
