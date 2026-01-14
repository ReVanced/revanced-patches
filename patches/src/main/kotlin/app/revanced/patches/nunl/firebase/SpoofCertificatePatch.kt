package app.revanced.patches.nunl.firebase

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Spoof certificate` by creatingBytecodePatch(
    description = "Spoofs the X-Android-Cert header to allow push messages.",
) {
    compatibleWith("nl.sanomamedia.android.nu")

    apply {
        getFingerprintHashForPackageMethods().forEach {
            it.returnEarly("eae41fc018df2731a9b6ae1ac327da44a288667b")
        }
    }
}
