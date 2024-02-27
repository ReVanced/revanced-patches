package app.revanced.patches.youtube.misc.minimizedplayback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.KidsMinimizedPlaybackPolicyControllerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.MinimizedPlaybackSettingsFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.fingerprints.PiPControllerFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.youtube.utils.integrations.IntegrationsPatch
import app.revanced.patches.youtube.utils.playertype.PlayerTypeHookPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Enable minimized playback",
    description = "Enables minimized and background playback.",
    dependencies = [
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object MinimizedPlaybackPatch : BytecodePatch(
    setOf(
        KidsMinimizedPlaybackPolicyControllerFingerprint,
        MinimizedPlaybackManagerFingerprint,
        MinimizedPlaybackSettingsFingerprint,
        PiPControllerFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        KidsMinimizedPlaybackPolicyControllerFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "return-void"
                )
            }
        } ?: throw KidsMinimizedPlaybackPolicyControllerFingerprint.exception

        MinimizedPlaybackManagerFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        invoke-static {}, $INTEGRATIONS_METHOD_REFERENCE
                        move-result v0
                        return v0
                        """
                )
            }
        } ?: throw MinimizedPlaybackManagerFingerprint.exception

        MinimizedPlaybackSettingsFingerprint.result?.let {
            it.mutableMethod.apply {
                val booleanCalls = implementation!!.instructions.withIndex()
                    .filter { instruction ->
                        ((instruction.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z"
                    }

                val booleanIndex = booleanCalls.elementAt(1).index
                val booleanMethod =
                    context.toMethodWalker(this)
                        .nextMethod(booleanIndex, true)
                        .getMethod() as MutableMethod

                booleanMethod.addInstructions(
                    0, """
                        const/4 v0, 0x1
                        return v0
                        """
                )
            }
        } ?: throw MinimizedPlaybackSettingsFingerprint.exception

        PiPControllerFingerprint.result?.let {
            val targetMethod = context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                .getMethod() as MutableMethod

            targetMethod.apply {
                val targetRegister = getInstruction<TwoRegisterInstruction>(0).registerA

                addInstruction(
                    1,
                    "const/4 v$targetRegister, 0x1"
                )
            }
        } ?: throw PiPControllerFingerprint.exception
    }

    private const val INTEGRATIONS_METHOD_REFERENCE =
        "$MISC_PATH/MinimizedPlaybackPatch;->isPlaybackNotShort()Z"
}
