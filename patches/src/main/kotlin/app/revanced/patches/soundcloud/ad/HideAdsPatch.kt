package app.revanced.patches.soundcloud.ad

import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.soundcloud.shared.featureConstructorMethod

@Suppress("unused")
val hideAdsPatch = bytecodePatch("Hide ads") {
    compatibleWith("com.soundcloud.android"("2025.05.27-release"))

    apply {
        // Enable a preset feature to disable audio ads by modifying the JSON server response.
        // This method is the constructor of a class representing a "Feature" object parsed from JSON data.
        // p1 is the name of the feature.
        // p2 is true if the feature is enabled, false otherwise.
        featureConstructorMethod.apply {
            val afterCheckNotNullIndex = 2
            addInstructionsWithLabels(
                afterCheckNotNullIndex,
                """
                    const-string v0, "no_audio_ads"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :skip
                    const/4 p2, 0x1
                """,
                ExternalLabel("skip", getInstruction(afterCheckNotNullIndex)),
            )
        }

        // Overwrite the JSON response from the server to a paid plan, which hides all ads in the app.
        // This does not enable paid features, as they are all checked for on the backend.
        // This method is the constructor of a class representing a "UserConsumerPlan" object parsed from JSON data.
        // p1 is the "currentTier" value, dictating which features to enable in the app.
        // p4 is the "consumerPlanUpsells" value, a list of plans to try to sell to the user.
        // p5 is the "currentConsumerPlan" value, the type of plan currently subscribed to.
        // p6 is the "currentConsumerPlanTitle" value, the name of the plan currently subscribed to, shown to the user.
        userConsumerPlanConstructorMethod.addInstructions(
            0,
            """
                const-string p1, "high_tier"
                new-instance p4, Ljava/util/ArrayList;
                invoke-direct {p4}, Ljava/util/ArrayList;-><init>()V
                const-string p5, "go-plus"
                const-string p6, "SoundCloud Go+"
            """,
        )

        // Prevent verification of an HTTP header containing the user's current plan, which would contradict the previous patch.

        val conditionIndex = interceptMethodMatch[-1] + 1
        interceptMethodMatch.method.addInstruction(
            conditionIndex,
            "return-object p1",
        )
    }
}
