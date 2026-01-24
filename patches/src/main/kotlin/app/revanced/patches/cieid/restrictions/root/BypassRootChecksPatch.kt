package app.revanced.patches.cieid.restrictions.root

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Bypass root checks` by creatingBytecodePatch(
    description = "Removes the restriction to use the app with root permissions or on a custom ROM.",
) {
    compatibleWith("it.ipzs.cieid")

    apply {
        checkRootMethod.returnEarly()
    }
}
