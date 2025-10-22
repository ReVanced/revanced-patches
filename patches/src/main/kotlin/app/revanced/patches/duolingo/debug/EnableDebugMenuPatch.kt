package app.revanced.patches.duolingo.debug

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val enableDebugMenuPatch = bytecodePatch(
    name = "Enable debug menu",
    use = false
) {
    compatibleWith("com.duolingo")

    execute {
        // It seems all categories are allowed on release. Force this on anyway.
        debugCategoryAllowOnReleaseBuildsFingerprint.method.returnEarly(true)

        // Change build config debug build flag.
        buildConfigProviderConstructorFingerprint.match(
            buildConfigProviderToStringFingerprint.classDef
        ).let {
            val index = it.patternMatch!!.startIndex

            it.method.apply {
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstruction(
                    index + 1,
                    "const/4 v$register, 0x1"
                )
            }
        }
    }
}
