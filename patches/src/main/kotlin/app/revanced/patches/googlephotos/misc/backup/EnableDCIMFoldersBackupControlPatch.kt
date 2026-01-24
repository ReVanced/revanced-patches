package app.revanced.patches.googlephotos.misc.backup

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Enable DCIM folders backup control` by creatingBytecodePatch(
    description = "Disables always on backup for the Camera and other DCIM folders, allowing you to control backup " +
        "for each folder individually. This will make the app default to having no folders backed up.",
    use = false,
) {
    compatibleWith("com.google.android.apps.photos")

    apply {
        isDCIMFolderBackupControlMethod.returnEarly(false)
    }
}
