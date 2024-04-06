package app.revanced.patches.tumblr.featureflags

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.tumblr.featureflags.fingerprints.GetFeatureValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

@Patch(description = "Forcibly set the value of A/B testing features of your choice.")
object OverrideFeatureFlagsPatch : BytecodePatch(
    setOf(GetFeatureValueFingerprint)
) {
    /**
     * Override a feature flag with a value.
     *
     * @param name The name of the feature flag to override.
     * @param value The value to override the feature flag with.
     */
    @Suppress("KDocUnresolvedReference")
    internal lateinit var addOverride: (name: String, value: String) -> Unit private set

    override fun execute(context: BytecodeContext) = GetFeatureValueFingerprint.result?.let {
        val configurationClass = it.method.definingClass
        val featureClass = it.method.parameterTypes[0].toString()

        // The method we want to inject into does not have enough registers, so we inject a helper method
        // and inject more instructions into it later, see addOverride.
        // This is not in an integration since the unused variable would get compiled away and the method would
        // get compiled to only have one register, which is not enough for our later injected instructions.
        val helperMethod = ImmutableMethod(
            it.method.definingClass,
            "getValueOverride",
            listOf(ImmutableMethodParameter(featureClass, null, "feature")),
            "Ljava/lang/String;",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            null,
            null,
            MutableMethodImplementation(4)
        ).toMutable().apply {
            // This is the equivalent of
            //   String featureName = feature.toString()
            //   <inject more instructions here later>
            //   return null
            addInstructions(
                0,
                """
                    # toString() the enum value
                    invoke-virtual {p1}, $featureClass->toString()Ljava/lang/String;
                    move-result-object v0
                    
                    # !!! If you add more instructions above this line, update helperInsertIndex below!
                    # Here we will insert one compare & return for every registered Feature override
                    # This is done below in the addOverride function
                    
                    # If none of the overrides returned a value, we should return null
                    const/4 v0, 0x0
                    return-object v0
                """
            )
        }.also { helperMethod ->
            it.mutableClass.methods.add(helperMethod)
        }

        // Here we actually insert the hook to call our helper method and return its value if it returns not null
        // This is equivalent to
        //   String forcedValue = getValueOverride(feature)
        //   if (forcedValue != null) return forcedValue
        val getFeatureIndex = it.scanResult.patternScanResult!!.startIndex
        it.mutableMethod.addInstructionsWithLabels(
            getFeatureIndex,
            """
                    # Call the Helper Method with the Feature
                    invoke-virtual {p0, p1}, $configurationClass->getValueOverride($featureClass)Ljava/lang/String;
                    move-result-object v0
                    # If it returned null, skip
                    if-eqz v0, :is_null
                    # If it didnt return null, return that string
                    return-object v0
                    
                    # If our override helper returned null, we let the function continue normally
                    :is_null
                    nop
                """
        )

        val helperInsertIndex = 2
        addOverride = { name, value ->
            // For every added override, we add a few instructions in the middle of the helper method
            // to check if the feature is the one we want and return the override value if it is.
            // This is equivalent to
            //   if (featureName == {name}) return {value}
            helperMethod.addInstructionsWithLabels(
                helperInsertIndex,
                """
                    # v0 is still the string name of the currently checked feature from above
                    # Compare the current string with the override string
                    const-string v1, "$name"
                    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v1
                    # If the current string is the one we want to override, we return the override value
                    if-eqz v1, :no_override
                    const-string v1, "$value"
                    return-object v1
                    # Else we just continue...
                    :no_override
                    nop
                """
            )
        }
    } ?: throw GetFeatureValueFingerprint.exception
}