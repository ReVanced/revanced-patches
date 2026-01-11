package app.revanced.patches.strava.distractions

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableBooleanEncodedValue.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.immutable.value.ImmutableBooleanEncodedValue

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/strava/HideDistractionsPatch;"
private const val MODULAR_ENTRY_CLASS_DESCRIPTOR = "Lcom/strava/modularframework/data/ModularEntry;"

@Suppress("unused")
val hideDistractionsPatch = bytecodePatch(
    name = "Hide distractions",
    description = "Hides elements that are not essential.",
) {
    compatibleWith("com.strava")

    extendWith("extensions/strava.rve")

    val upsellingOption = booleanOption(
        key = "upselling",
        title = "Upselling",
        description = "Elements that suggest you subscribe.",
        default = true,
        required = true,
    )

    val promoOption = booleanOption(
        key = "promo",
        title = "Promotions",
        default = true,
        required = true,
    )

    val followSuggestionsOption = booleanOption(
        key = "followSuggestions",
        title = "Who to Follow",
        description = "Popular athletes, followers, people near you etc.",
        default = true,
        required = true,
    )

    val challengeSuggestionsOption = booleanOption(
        key = "challengeSuggestions",
        title = "Suggested Challenges",
        description = "Random challenges Strava wants you to join.",
        default = true,
        required = true,
    )

    val joinChallengeOption = booleanOption(
        key = "joinChallenge",
        title = "Join Challenge",
        description = "Challenges your follows have joined.",
        default = false,
        required = true,
    )

    val joinClubOption = booleanOption(
        key = "joinClub",
        title = "Joined a club",
        description = "Clubs your follows have joined.",
        default = false,
        required = true,
    )

    val activityLookbackOption = booleanOption(
        key = "activityLookback",
        title = "Your activity from X years ago",
        default = false,
        required = true,
    )

    execute {
        // Write option values into extension class.
        classBy { it.type == EXTENSION_CLASS_DESCRIPTOR }!!.mutableClass.apply {
            arrayOf(
                upsellingOption,
                promoOption,
                followSuggestionsOption,
                challengeSuggestionsOption,
                joinChallengeOption,
                joinClubOption,
                activityLookbackOption,
            ).forEach { option ->
                staticFields.first { field -> field.name == option.key }.initialValue =
                    ImmutableBooleanEncodedValue.forBoolean(option.value == true).toMutable()
            }
        }

        // Intercept all `getModules()` calls to check whether they should be hidden.
        classes.filter { it.interfaces.contains(MODULAR_ENTRY_CLASS_DESCRIPTOR) }.forEach { modularEntryClass ->
            modularEntryClass.virtualMethods.first { method ->
                method.name == "getModules" && method.parameterTypes.isEmpty()
            }.toMutable().apply {
                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->hide($MODULAR_ENTRY_CLASS_DESCRIPTOR)Z
                        move-result v0
                        if-eqz v0, :original
                        invoke-static { }, Ljava/util/Collections;->emptyList()Ljava/util/List;
                        move-result-object v0
                        return-object v0
                    """,
                    ExternalLabel("original", instructions[0])
                )
            }
        }
    }
}
