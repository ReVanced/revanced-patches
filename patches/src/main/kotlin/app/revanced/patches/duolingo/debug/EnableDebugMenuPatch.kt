package app.revanced.patches.duolingo.debug

import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val enableDebugMenuPatch = bytecodePatch(
    name = "Enable debug menu",
    use = false
) {
    compatibleWith("com.duolingo")

    apply {
        // It seems all categories are allowed on release. Force this on anyway.
        debugCategoryAllowOnReleaseBuildsMethod.returnEarly(true)

        // Change build config debug build flag.
        buildConfigProviderConstructorMethod.match( // TODO
            buildConfigProviderToStringMethod.classDef
        ).let {
            val index = it.patternMatch.startIndex // TODO

            it.apply {
                val register = getInstruction<OneRegisterInstruction>(index).registerA // TODO
                addInstruction(
                    index + 1,
                    "const/4 v$register, 0x1"
                )
            }
        }
    }
}
