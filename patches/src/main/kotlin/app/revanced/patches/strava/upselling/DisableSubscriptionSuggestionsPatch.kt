package app.revanced.patches.strava.upselling

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.strava.distractions.hideDistractionsPatch

@Suppress("unused")
@Deprecated("Superseded by \"Hide distractions\" patch", ReplaceWith("hideDistractionsPatch"))
val disableSubscriptionSuggestionsPatch = bytecodePatch(
    name = "Disable subscription suggestions",
) {
    compatibleWith("com.strava")

    dependsOn(hideDistractionsPatch.apply {
        options["upselling"] = true
        options["promo"] = false
        options["followSuggestions"] = false
        options["challengeSuggestions"] = false
        options["joinChallenge"] = false
        options["joinClub"] = false
        options["activityLookback"] = false
    })
}
