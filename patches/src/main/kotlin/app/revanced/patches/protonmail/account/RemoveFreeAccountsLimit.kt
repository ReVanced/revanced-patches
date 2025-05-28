package app.revanced.patches.protonmail.account

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findElementByAttributeValueOrThrow

@Suppress("unused")
val removeFreeAccountsLimit = resourcePatch(
    name = "Remove free accounts limit",
    description = "Removes the limit for maximum free accounts logged in.",
) {
    compatibleWith("ch.protonmail.android")

    execute {
        document("res/values/integers.xml").use { document ->
            document.documentElement.childNodes.findElementByAttributeValueOrThrow(
                "name",
                "core_feature_auth_user_check_max_free_user_count",
            ).textContent = Int.MAX_VALUE.toString()
        }
    }
}