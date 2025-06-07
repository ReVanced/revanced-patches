package app.revanced.patches.googlephotos.misc.backup

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableDCIMFoldersBackupControlPatch = bytecodePatch(
    name = "Enable DCIM folders backup control",
    description = "Allows controlling whether backup is enabled for each DCIM folder. " +
        "This will by make the app default to having no folders backed up, as it allows controlling " +
        "the backup even for the Camera folder, which is normally always backed up.",
    use = false,
) {
    compatibleWith("com.google.android.apps.photos")

    execute {
        isDCIMFolderBackupControlDisabled.method.returnEarly(false)
    }
}
