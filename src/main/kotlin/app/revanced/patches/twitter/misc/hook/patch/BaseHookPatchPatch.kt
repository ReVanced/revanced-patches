package app.revanced.patches.twitter.misc.hook.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.twitter.misc.hook.json.patch.JsonHookPatch

abstract class BaseHookPatchPatch(private val hookClassDescriptor: String) : BytecodePatch() {
    override fun execute(context: BytecodeContext) = try {
        JsonHookPatch.hooks.addHook(JsonHookPatch.Hook(context, hookClassDescriptor))

        PatchResultSuccess()
    } catch (ex: Exception) {
        PatchResultError(ex)
    }
}