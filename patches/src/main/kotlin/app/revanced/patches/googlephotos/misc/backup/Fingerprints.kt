package app.revanced.patches.googlephotos.misc.backup

import app.revanced.patcher.fingerprint

internal val isDCIMFolderBackupControlDisabled by fingerprint {
    returns("Z")
    strings("/dcim", "/mars_files/")
}
