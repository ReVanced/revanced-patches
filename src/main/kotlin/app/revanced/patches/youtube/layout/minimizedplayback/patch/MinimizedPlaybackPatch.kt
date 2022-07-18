package app.revanced.patches.youtube.layout.minimizedplayback.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.data.impl.toMethodWalker
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.layout.minimizedplayback.fingerprints.MinimizedPlaybackKidsFingerprint
import app.revanced.patches.youtube.layout.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.layout.minimizedplayback.fingerprints.MinimizedPlaybackSettingsFingerprint
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference


@Patch
@Name("minimized-playback")
@Description("Enables minimized and background playback.")
@MinimizedPlaybackCompatibility
@Version("0.0.1")
class MinimizedPlaybackPatch : BytecodePatch(
    listOf(
        MinimizedPlaybackKidsFingerprint, MinimizedPlaybackManagerFingerprint, MinimizedPlaybackSettingsFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        MinimizedPlaybackManagerFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                const/4 v0, 0x1
                return v0
                """
        )

        val method = MinimizedPlaybackSettingsFingerprint.result!!.mutableMethod
        val booleanCalls = method.implementation!!.instructions.withIndex()
            .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

        val settingsBooleanIndex = booleanCalls.elementAt(1).index
        val settingsBooleanMethod =
            data.toMethodWalker(method).nextMethod(settingsBooleanIndex, true).getMethod() as MutableMethod

        settingsBooleanMethod.addInstructions(
            0, """
                const/4 v0, 0x1
                return v0
                """
        )

        MinimizedPlaybackKidsFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                return-void
                """
        )

        return PatchResultSuccess()
    }
}
