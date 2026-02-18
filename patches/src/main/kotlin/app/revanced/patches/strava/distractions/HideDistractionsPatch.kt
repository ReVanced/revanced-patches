package app.revanced.patches.strava.distractions

import app.revanced.com.android.tools.smali.dexlib2.iface.value.MutableBooleanEncodedValue.Companion.toMutable
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableClassDef
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.strava.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.immutable.value.ImmutableBooleanEncodedValue
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/strava/HideDistractionsPatch;"
private const val MODULAR_FRAMEWORK_CLASS_DESCRIPTOR_PREFIX = "Lcom/strava/modularframework"

private const val METHOD_SUFFIX = "\$original"

private data class FilterablePropertyFingerprint(
    val name: String,
    val parameterTypes: List<String> = listOf(),
)

private val fingerprints = arrayOf(
    FilterablePropertyFingerprint("ChildrenEntries"),
    FilterablePropertyFingerprint("Entries"),
    FilterablePropertyFingerprint("Field", listOf("Ljava/lang/String;")),
    FilterablePropertyFingerprint("Fields"),
    FilterablePropertyFingerprint("MenuItems"),
    FilterablePropertyFingerprint("Modules"),
    FilterablePropertyFingerprint("Properties"),
    FilterablePropertyFingerprint("StateMap"),
    FilterablePropertyFingerprint("Submodules"),
)

@Suppress("unused")
val hideDistractionsPatch = bytecodePatch(
    name = "Hide distractions",
    description = "Hides elements that are not essential.",
) {
    compatibleWith("com.strava")

    dependsOn(sharedExtensionPatch)

    val logger = Logger.getLogger(this::class.java.name)

    val options = mapOf(
        "upselling" to booleanOption(
            name = "Upselling",
            description = "Elements that suggest you subscribe.",
            default = true,
            required = true,
        ),
        "promo" to booleanOption(
            name = "Promotions",
            description = "Elements that promote features, challenges, clubs, etc.",
            default = true,
            required = true,
        ),
        "followSuggestions" to booleanOption(
            name = "Who to Follow",
            description = "Popular athletes, followers, people near you etc.",
            default = true,
            required = true,
        ),
        "challengeSuggestions" to booleanOption(
            name = "Suggested Challenges",
            description = "Random challenges Strava wants you to join.",
            default = true,
            required = true,
        ),
        "joinChallenge" to booleanOption(
            name = "Join Challenge",
            description = "Challenges your follows have joined.",
            default = false,
            required = true,
        ),
        "joinClub" to booleanOption(
            name = "Joined a club",
            description = "Clubs your follows have joined.",
            default = false,
            required = true,
        ),
        "activityLookback" to booleanOption(
            name = "Activity lookback",
            description = "Your activity from X years ago",
            default = false,
            required = true,
        ),
    )

    apply {
        // region Write option values into extension class.

        val extensionClass = firstClassDef { type == EXTENSION_CLASS_DESCRIPTOR }.apply {
            options.forEach { (key, option) ->
                staticFields.first { field -> field.name == key }.initialValue =
                    ImmutableBooleanEncodedValue.forBoolean(option.value == true).toMutable()
            }
        }

        // endregion

        // region Intercept all classes' property getter calls.

        fun MutableMethod.cloneAndIntercept(
            classDef: MutableClassDef,
            extensionMethodName: String,
            extensionMethodParameterTypes: List<String>,
        ) {
            val extensionMethodReference = ImmutableMethodReference(
                EXTENSION_CLASS_DESCRIPTOR,
                extensionMethodName,
                extensionMethodParameterTypes,
                returnType,
            )

            if (extensionClass.directMethods.none { method ->
                    MethodUtil.methodSignaturesMatch(method, extensionMethodReference)
                }
            ) {
                logger.info { "Skipped interception of $this due to missing $extensionMethodReference" }
                return
            }

            classDef.virtualMethods -= this

            val clone = ImmutableMethod.of(this).toMutable()

            classDef.virtualMethods += clone

            if (implementation != null) {
                val registers = List(extensionMethodParameterTypes.size) { index -> "p$index" }.joinToString(
                    separator = ",",
                    prefix = "{",
                    postfix = "}",
                )

                clone.addInstructions(
                    0,
                    """
                        invoke-static $registers, $extensionMethodReference
                        move-result-object v0
                        return-object v0
                    """,
                )

                logger.fine { "Intercepted $this with $extensionMethodReference" }
            }

            name += METHOD_SUFFIX

            classDef.virtualMethods += this
        }

        classDefs.filter { it.type.startsWith(MODULAR_FRAMEWORK_CLASS_DESCRIPTOR_PREFIX) }.forEach { classDef ->
            val mutableClassDef by lazy { classDefs.getOrReplaceMutable(classDef) }

            classDef.virtualMethods.forEach { method ->
                fingerprints.find { fingerprint ->
                    method.name == "get${fingerprint.name}" && method.parameterTypes == fingerprint.parameterTypes
                }?.let { fingerprint ->
                    // Upcast to the interface if this is an interface implementation.
                    val parameterType = classDef.interfaces.find {
                        classDefs.find { interfaceDef -> interfaceDef.type == it }?.virtualMethods?.any { interfaceMethod ->
                            MethodUtil.methodSignaturesMatch(interfaceMethod, method)
                        } == true
                    } ?: classDef.type

                    mutableClassDef.firstMethod(method).cloneAndIntercept(
                        mutableClassDef,
                        "filter${fingerprint.name}",
                        listOf(parameterType) + fingerprint.parameterTypes,
                    )
                }
            }
        }

        // endregion
    }
}
