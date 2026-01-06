package app.revanced.patches.reddit.customclients.continuum.api

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.findElementByAttributeValueOrThrow

val redditApiResourcePatch = resourcePatch(
    name = "Reddit API override (resource)",
    description = "Overrides Reddit client ID in strings.xml",
) {
    compatibleWith("org.cygnusx1.continuum", "org.cygnusx1.continuum.debug")

    execute {
        document("res/values/strings.xml").use { document ->
            document.documentElement.childNodes.findElementByAttributeValueOrThrow(
                "name",
                "default_client_id"
            ).textContent = Constants.NEW_CLIENT_ID
        }
    }
}
