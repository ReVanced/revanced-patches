package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.patch.bytecodePatch


@Deprecated("Function was moved", ReplaceWith("app.revanced.patches.shared.layout.theme.lithoColorOverrideHook"))
@Suppress("unused")
lateinit var lithoColorOverrideHook: (targetMethodClass: String, targetMethodName: String) -> Unit
    private set

@Deprecated("Patch was moved", ReplaceWith("app.revanced.patches.shared.layout.theme.lithoColorHookPatch"))
@Suppress("unused")
val lithoColorHookPatch = bytecodePatch{
    dependsOn(app.revanced.patches.shared.layout.theme.lithoColorHookPatch)

    execute {
        lithoColorOverrideHook = app.revanced.patches.shared.layout.theme.lithoColorOverrideHook
    }
}
