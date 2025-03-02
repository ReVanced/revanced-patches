package app.revanced.patches.all.misc.directory

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.directory.documentsprovider.exportInternalDataDocumentsProviderPatch

@Suppress("unused")
@Deprecated(
    "Superseded by internalDataDocumentsProviderPatch",
    ReplaceWith("internalDataDocumentsProviderPatch"),
)
val changeDataDirectoryLocationPatch = bytecodePatch(
    // name = "Change data directory location",
    description = "Changes the data directory in the application from " +
        "the app internal storage directory to /sdcard/android/data accessible by root-less devices." +
        "Using this patch can cause unexpected issues with some apps.",
    use = false,
) {
    dependsOn(exportInternalDataDocumentsProviderPatch)
}
