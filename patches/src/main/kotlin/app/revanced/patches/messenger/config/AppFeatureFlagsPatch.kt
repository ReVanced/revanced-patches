package app.revanced.patches.messenger.config

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/** Based on [lithoFilterPatch]. */

lateinit var addAppFeatureFlagsOverrider: (String) -> Unit
    private set

@Suppress("unused")
val appFeatureFlagsPatch = bytecodePatch(
    description = "Hooks the method controlling which app features are enabled."
) {
    dependsOn(sharedExtensionPatch)

    var overriderCount = 0

    execute {
        appFeatureFlagsPatchFingerprint.method.apply {
            removeInstructions(2, 4) // Remove dummy overrider.

            addAppFeatureFlagsOverrider = { classDescriptor ->
                addInstructions(
                    2,
                    """
                        new-instance v1, $classDescriptor
                        invoke-direct { v1 }, $classDescriptor-><init>()V
                        const/16 v2, ${overriderCount++}
                        aput-object v1, v0, v2
                    """,
                )
            }
        }

        getMobileConfigBoolFingerprint.method.apply {
            val returnIndex = getMobileConfigBoolFingerprint.patternMatch!!.startIndex
            val returnRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA

            addInstructions(
                returnIndex,
                """
                    invoke-static { p1, p2, v$returnRegister }, $EXTENSION_CLASS_DESCRIPTOR->overrideBooleanFlag(JZ)Z
                    move-result v$returnRegister
                """
            )
        }
    }

    finalize {
        appFeatureFlagsPatchFingerprint.method.replaceInstruction(0, "const/16 v0, $overriderCount")
    }
}
