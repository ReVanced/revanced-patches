package app.revanced.patches.strava.upselling

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.strava.distractions.hideDistractionsPatch

@Suppress("unused")
@Deprecated("Superseded by \"Hide distractions\" patch", ReplaceWith("hideDistractionsPatch"))
val disableSubscriptionSuggestionsPatch = bytecodePatch(
    name = "Disable subscription suggestions",
) {
    compatibleWith("com.strava")

    dependsOn(
        hideDistractionsPatch.apply {
            options["Upselling"] = true
            options["Promotions"] = false
            options["Who to Follow"] = false
            options["Suggested Challenges"] = false
            options["Join Challenge"] = false
            options["Joined a club"] = false
            options["Your activity from X years ago"] = false
        },
    )
}
