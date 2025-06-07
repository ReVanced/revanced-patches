package app.revanced.patches.googlephotos.misc.backup

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableDCIMFoldersBackupControlPatch = bytecodePatch(
    name = "Enable DCIM folders backup control",
    description = "Disables always on backup for the Camera and other DCIM folders, allowing you to control backup " +
            "for each folder individually. This will make the app default to having no folders backed up.",
    use = false,
) {
    compatibleWith("com.google.android.apps.photos")

    execute {
        isDCIMFolderBackupControlDisabled.method.returnEarly(false)
    }
}
