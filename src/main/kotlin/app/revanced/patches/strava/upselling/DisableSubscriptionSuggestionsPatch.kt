package app.revanced.patches.strava.upselling

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.strava.upselling.fingerprints.GetModulesFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    name = "Disable subscription suggestions",
    compatiblePackages = [CompatiblePackage("com.strava", ["320.12"])]
)
@Suppress("unused")
object DisableSubscriptionSuggestionsPatch : BytecodePatch(
    setOf(GetModulesFingerprint)
) {
    private const val HELPER_METHOD_NAME = "getModulesIfNotUpselling"
    private const val PAGE_SUFFIX = "_upsell"
    private const val LABEL = "original"

    override fun execute(context: BytecodeContext) = GetModulesFingerprint.result?.let { result ->
        val className = result.classDef.type
        val originalMethod = result.mutableMethod
        val returnType = originalMethod.returnType

        result.mutableClass.methods.add(ImmutableMethod(
            className,
            HELPER_METHOD_NAME,
            emptyList(),
            returnType,
            AccessFlags.PRIVATE.value,
            null,
            null,
            MutableMethodImplementation(3)
        ).toMutable().apply {
            addInstructions(
                """
                    iget-object v0, p0, $className->page:Ljava/lang/String;
                    const-string v1, "$PAGE_SUFFIX"
                    invoke-virtual {v0, v1}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :$LABEL
                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                    move-result-object v0
                    return-object v0
                    :$LABEL
                    iget-object v0, p0, $className->modules:Ljava/util/List;
                    return-object v0
                """
            )
        })

        val getModulesIndex = result.scanResult.patternScanResult!!.startIndex
        with(originalMethod) {
            removeInstruction(getModulesIndex)
            addInstructions(
                getModulesIndex,
                """
                    invoke-direct {p0}, $className->$HELPER_METHOD_NAME()$returnType
                    move-result-object v0
                """
            )
        }
    } ?: throw GetModulesFingerprint.exception
}
