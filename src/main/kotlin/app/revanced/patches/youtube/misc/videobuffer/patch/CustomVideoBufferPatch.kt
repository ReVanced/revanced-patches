package app.revanced.patches.youtube.misc.videobuffer.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.videobuffer.annotations.CustomVideoBufferCompatibility
import app.revanced.patches.youtube.misc.videobuffer.fingerprints.MaxBufferFingerprint
import app.revanced.patches.youtube.misc.videobuffer.fingerprints.PlaybackBufferFingerprint
import app.revanced.patches.youtube.misc.videobuffer.fingerprints.ReBufferFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("custom-video-buffer")
@Description("Lets you change the buffers of videos.")
@DependsOn([SettingsPatch::class])
@CustomVideoBufferCompatibility
@Version("0.0.1")
class CustomVideoBufferPatch : BytecodePatch(
    listOf(
        MaxBufferFingerprint, PlaybackBufferFingerprint, ReBufferFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            PreferenceScreen(
                "revanced_custom_video_buffer",
                StringResource("revanced_custom_video_buffer_title", "Video buffer settings"),
                listOf(
                    TextPreference(
                        "revanced_pref_max_buffer_ms",
                        StringResource("revanced_pref_max_buffer_ms_title", "Maximum buffer size"),
                        InputType.NUMBER,
                        "120000",
                        StringResource(
                            "revanced_pref_max_buffer_ms_summary",
                            "The maximum size of a buffer for playback"
                        )
                    ),
                    TextPreference(
                        "revanced_pref_buffer_for_playback_ms",
                        StringResource("revanced_pref_buffer_for_playback_ms_title", "Maximum buffer for playback"),
                        InputType.NUMBER,
                        "2500",
                        StringResource(
                            "revanced_pref_buffer_for_playback_ms_summary",
                            "Maximum size of a buffer for playback"
                        )
                    ),
                    TextPreference(
                        "revanced_pref_buffer_for_playback_after_rebuffer_ms",
                        StringResource(
                            "revanced_pref_buffer_for_playback_after_rebuffer_ms_title",
                            "Maximum buffer for playback after rebuffer"
                        ),
                        InputType.NUMBER,
                        "5000",
                        StringResource(
                            "revanced_pref_buffer_for_playback_after_rebuffer_ms_summary",
                            "Maximum size of a buffer for playback after rebuffering"
                        )
                    )
                ),
                StringResource("revanced_custom_video_buffer_summary", "Custom settings for video buffer")
            )
        )
        BufferType.values().forEach { type ->
            type.hook(context)
        }

        return PatchResultSuccess()
    }

    /**
     * The type of buffer.
     *
     * @param patchInfo The corresponding information to patch the buffer type.
     * @param preparation Optional preparation before patching.
     */
    private enum class BufferType(
        private val patchInfo: PatchInfo,
        private val preparation: (BytecodeContext.() -> Unit)? = null,
    ) {

        PLAYBACK(PatchInfo(PlaybackBufferFingerprint, "getPlaybackBuffer")),
        RE(PatchInfo(ReBufferFingerprint, "getReBuffer")),
        MAX(
            PatchInfo(
                MaxBufferFingerprint,
                "getMaxBuffer",
                PatchInfo.UnwrapInfo(true, -1)
            )
        );

        /**
         * Information about a patch.
         *
         * @param fingerprint The corresponding [MethodFingerprint] for the patch.
         * @param integrationsMethodName The corresponding name of the hooking method.
         * @param unwrapInfo Optional information on how to treat the [MethodFingerprint].
         */
        private class PatchInfo(
            val fingerprint: MethodFingerprint,
            val integrationsMethodName: String,
            val unwrapInfo: UnwrapInfo? = null
        ) {
            /**
             * Information on how to treat a [MethodFingerprint].
             *
             * @param forEndIndex Whether to retrieve information from the [MethodFingerprint]
             * from the end or start index of its pattern scan result.
             * @param offset An additional offset to [forEndIndex].
             */
            class UnwrapInfo(val forEndIndex: Boolean = false, val offset: Int = 0)
        }

        fun hook(context: BytecodeContext) {
            /**
             * The resulting instruction info for unwrapping [MethodFingerprint].
             *
             * @param index The index of the instruction.
             * @param register The register of the instruction.
             */
            data class InstructionResult(val index: Int, val register: Int)

            /***
             * The result of unwrapping [MethodFingerprint].
             *
             * @param method The method which was retrieved from the [MethodFingerprint].
             * @param instructionResult The resulting instruction info for unwrapping [MethodFingerprint].
             */
            data class UnwrapResult(val method: MutableMethod, val instructionResult: InstructionResult)

            fun MethodFingerprint.unwrap(unwrapInfo: PatchInfo.UnwrapInfo? = null): UnwrapResult {
                val result = this.result!!
                val method = result.mutableMethod
                val scanResult = result.scanResult.patternScanResult!!
                val index = (
                        if (unwrapInfo?.forEndIndex == true)
                            scanResult.endIndex
                        else
                            scanResult.startIndex
                        ) + (unwrapInfo?.offset ?: 0)

                val register = (method.instruction(index) as OneRegisterInstruction).registerA

                return UnwrapResult(method, InstructionResult(index, register))
            }

            preparation?.invoke(context)

            val (method, result) = patchInfo.fingerprint.unwrap(patchInfo.unwrapInfo)
            val (index, register) = result

            method.addInstructions(
                index + 1,
                """
                   invoke-static {}, Lapp/revanced/integrations/patches/VideoBufferPatch;->${patchInfo.integrationsMethodName}()I
                   move-result v$register
                """
            )
        }
    }
}
