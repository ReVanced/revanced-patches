package app.revanced.patches.strava.distractions

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.proxy.mutableTypes.encodedValue.MutableBooleanEncodedValue.Companion.toMutable
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.value.ImmutableBooleanEncodedValue

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/strava/HideDistractionsPatch;"
private const val MODULAR_FRAMEWORK_PREFIX = "Lcom/strava/modularframework/data/"
private const val MODULAR_ENTRY_CLASS_DESCRIPTOR = "${MODULAR_FRAMEWORK_PREFIX}ModularEntry;"

private const val METHOD_SUFFIX = "\$original"
private const val GET_MODULES_NAME = "getModules"
private const val GET_MODULES_ORIGINAL_NAME = "$GET_MODULES_NAME$METHOD_SUFFIX"
private const val GET_SUBMODULES_NAME = "getSubmodules"
private const val GET_SUBMODULES_ORIGINAL_NAME = "$GET_SUBMODULES_NAME$METHOD_SUFFIX"

@Suppress("unused")
val hideDistractionsPatch = bytecodePatch(
    name = "Hide distractions",
    description = "Hides elements that are not essential.",
) {
    compatibleWith("com.strava")

    extendWith("extensions/strava.rve")

    // region Options.

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

    // endregion

    execute {
        // region Write option values into extension class.

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

        // endregion

        // region Copy interface's `getModules()` and rename it.

        classBy { it.type == MODULAR_ENTRY_CLASS_DESCRIPTOR }!!.mutableClass.apply {
            virtualMethods.first { method ->
                method.name == GET_MODULES_NAME && method.parameterTypes.isEmpty()
            }.let(ImmutableMethod::of).toMutable().apply {
                name = GET_MODULES_ORIGINAL_NAME
            }.let(virtualMethods::add)
        }

        // endregion

        // region Intercept all implementing classes' `getModules()` calls.

        classes
            .filter { it.interfaces.contains(MODULAR_ENTRY_CLASS_DESCRIPTOR) }
            .map { proxy(it).mutableClass }
            .forEach { modularEntryClass ->
                modularEntryClass.virtualMethods.first { method ->
                    method.name == GET_MODULES_NAME && method.parameterTypes.isEmpty()
                }.apply {
                    modularEntryClass.virtualMethods -= this

                    modularEntryClass.virtualMethods += ImmutableMethod.of(this).toMutable().apply {
                        removeInstructions(instructions.size)
                        addInstructions(
                            """
                                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->filterModules($MODULAR_ENTRY_CLASS_DESCRIPTOR)$returnType
                                move-result-object v0
                                return-object v0
                            """
                        )
                    }

                    name = GET_MODULES_ORIGINAL_NAME

                    modularEntryClass.virtualMethods += this
                }
            }

        // endregion

        // region Intercept all classes' `getSubmodules()` calls.

        classes.filter { classDef ->
                classDef.type.startsWith(MODULAR_FRAMEWORK_PREFIX) && classDef.virtualMethods.any { method ->
                    method.name == GET_SUBMODULES_NAME && method.parameterTypes.isEmpty()
                }
            }
            .map { proxy(it).mutableClass }
            .forEach { moduleClass ->
                moduleClass.virtualMethods.first { method ->
                    method.name == GET_SUBMODULES_NAME && method.parameterTypes.isEmpty()
                }.apply {
                    moduleClass.virtualMethods -= this

                    moduleClass.virtualMethods += ImmutableMethod.of(this).toMutable().apply {
                        removeInstructions(instructions.size)
                        addInstructions(
                            """
                                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->filterSubmodules($moduleClass)$returnType
                                move-result-object v0
                                return-object v0
                            """
                        )
                    }

                    name = GET_SUBMODULES_ORIGINAL_NAME

                    moduleClass.virtualMethods += this
                }
            }

        // endregion
    }
}
