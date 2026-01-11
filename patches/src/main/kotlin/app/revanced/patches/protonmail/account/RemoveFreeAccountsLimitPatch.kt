package app.revanced.patches.protonmail.account

import app.revanced.patcher.patch.creatingResourcePatch
import app.revanced.util.findElementByAttributeValueOrThrow

@Suppress("unused", "ObjectPropertyName")
val `Remove free accounts limit` by creatingResourcePatch(
    description = "Removes the limit for maximum free accounts logged in."
) {
    compatibleWith("ch.protonmail.android"("4.15.0"))

    apply {
        document("res/values/integers.xml").use { document ->
            document.documentElement.childNodes.findElementByAttributeValueOrThrow(
                "name",
                "core_feature_auth_user_check_max_free_user_count",
            ).textContent = Int.MAX_VALUE.toString()
        }
    }
}
