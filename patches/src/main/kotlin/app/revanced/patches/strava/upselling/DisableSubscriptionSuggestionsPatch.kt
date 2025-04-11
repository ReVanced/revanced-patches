package app.revanced.patches.strava.upselling

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Suppress("unused")
val disableSubscriptionSuggestionsPatch = bytecodePatch(
    name = "Disable subscription suggestions",
) {
    compatibleWith("com.strava")

    execute {
        val helperMethodName = "getModulesIfNotUpselling"
        val pageSuffix = "_upsell"
        val label = "original"

        val className = getModulesFingerprint.originalClassDef.type
        val originalMethod = getModulesFingerprint.method
        val returnType = originalMethod.returnType

        getModulesFingerprint.classDef.methods.add(
            ImmutableMethod(
                className,
                helperMethodName,
                emptyList(),
                returnType,
                AccessFlags.PRIVATE.value,
                null,
                null,
                MutableMethodImplementation(3),
            ).toMutable().apply {
                addInstructions(
                    """
                    iget-object v0, p0, $className->page:Ljava/lang/String;
                    const-string v1, "$pageSuffix"
                    invoke-virtual {v0, v1}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :$label
                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                    move-result-object v0
                    return-object v0
                    :$label
                    iget-object v0, p0, $className->modules:Ljava/util/List;
                    return-object v0
                """,
                )
            },
        )

        val getModulesIndex = getModulesFingerprint.patternMatch!!.startIndex
        with(originalMethod) {
            removeInstruction(getModulesIndex)
            addInstructions(
                getModulesIndex,
                """
                    invoke-direct {p0}, $className->$helperMethodName()$returnType
                    move-result-object v0
                """,
            )
        }
    }
}
