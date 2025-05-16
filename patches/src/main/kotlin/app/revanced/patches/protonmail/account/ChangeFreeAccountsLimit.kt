package app.revanced.patches.protonmail.account

import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findElementByAttributeValueOrThrow

private const val ACCOUNT_LIMIT = 10

@Suppress("unused")
val changeFreeAccountsLimit = resourcePatch(
    name = "Change free accounts limit",
    description = "Applies a custom limit for maximum free accounts logged in. Defaults to \"10\".",
) {
    compatibleWith("ch.protonmail.android")

    val limit by intOption(
        key = "limit",
        default = ACCOUNT_LIMIT,
        title = "Account limit",
        description = "The maximum number of free accounts allowed.",
    )

    execute {
        document("res/values/integers.xml").use { document ->
            document.documentElement.childNodes.findElementByAttributeValueOrThrow(
                "name",
                "core_feature_auth_user_check_max_free_user_count",
            ).textContent = limit.toString()
        }
    }
}