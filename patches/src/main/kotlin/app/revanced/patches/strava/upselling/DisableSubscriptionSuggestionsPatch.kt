package app.revanced.patches.strava.upselling

import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable

@Suppress("unused")
val `Disable subscription suggestions` by creatingBytecodePatch {
    compatibleWith("com.strava")

    apply {
        val helperMethodName = "getModulesIfNotUpselling"
        val pageSuffix = "_upsell"
        val label = "original"

        val className = getModulesMethod.classDef.type
        val originalMethod = getModulesMethod
        val returnType = originalMethod.returnType

        getModulesMethod.classDef.methods.add(
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

        val getModulesIndex = getModulesMethod.instructionMatches.first().index // TODO
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
