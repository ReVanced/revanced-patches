package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Alternative thumbnails",
    description = "Adds options to replace video thumbnails with still image captures of the video.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object AlternativeThumbnailsPatch : BytecodePatch(
    setOf(MessageDigestImageUrlParentFingerprint, CronetURLRequestCallbackOnResponseStartedFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/AlternativeThumbnailsPatch;"

    private lateinit var loadImageUrlMethod: MutableMethod
    private var loadImageUrlIndex = 0

    private lateinit var loadImageSuccessCallbackMethod: MutableMethod
    private var loadImageSuccessCallbackIndex = 0

    private lateinit var loadImageErrorCallbackMethod: MutableMethod
    private var loadImageErrorCallbackIndex = 0

    /**
     * @param highPriority If the hook should be called before all other hooks.
     */
    private fun addImageUrlHook(targetMethodClass: String, highPriority: Boolean) {
        loadImageUrlMethod.addInstructions(
            if (highPriority) 0 else loadImageUrlIndex, """
                    invoke-static { p1 }, $targetMethodClass->overrideImageURL(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object p1
                """
        )
        loadImageUrlIndex += 2
    }

    /**
     * If a connection completed, which includes normal 200 responses but also includes
     * status 404 and other error like http responses.
     */
    private fun addImageUrlSuccessCallbackHook(targetMethodClass: String) {
        loadImageSuccessCallbackMethod.addInstruction(
            loadImageSuccessCallbackIndex++,
            "invoke-static { p2 }, $targetMethodClass->handleCronetSuccess(Lorg/chromium/net/UrlResponseInfo;)V"
        )
    }

    /**
     * If a connection outright failed to complete any connection.
     */
    fun addImageUrlErrorCallbackHook(targetMethodClass: String) {
        loadImageErrorCallbackMethod.addInstruction(
            loadImageErrorCallbackIndex++,
            "invoke-static { p2, p3 }, $targetMethodClass->handleCronetFailure(Lorg/chromium/net/UrlResponseInfo;Ljava/io/IOException;)V"
        )
    }

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_alt_thumbnails_preference_screen",
                StringResource("revanced_alt_thumbnails_preference_screen_title", "Alternative thumbnails"),
                listOf(
                    SwitchPreference(
                        "revanced_alt_thumbnail",
                        StringResource("revanced_alt_thumbnail_title", "Enable alternative thumbnails"),
                        StringResource("revanced_alt_thumbnail_summary_on", "YouTube video stills shown"),
                        StringResource("revanced_alt_thumbnail_summary_off", "Original YouTube thumbnails shown")
                    ),
                    ListPreference(
                        "revanced_alt_thumbnail_type",
                        StringResource("revanced_alt_thumbnail_type_title", "Video time to take the still from"),
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entries",
                            listOf(
                                StringResource("revanced_alt_thumbnail_type_entry_1", "Beginning of video"),
                                StringResource("revanced_alt_thumbnail_type_entry_2", "Middle of video"),
                                StringResource("revanced_alt_thumbnail_type_entry_3", "End of video"),
                            )
                        ),
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entry_values",
                            listOf(
                                StringResource("revanced_alt_thumbnail_type_entry_value_1", "1"),
                                StringResource("revanced_alt_thumbnail_type_entry_value_2", "2"),
                                StringResource("revanced_alt_thumbnail_type_entry_value_3", "3"),
                            )
                        )
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_fast_quality",
                        StringResource("revanced_alt_thumbnail_fast_quality_title", "Use fast alternative thumbnails"),
                        StringResource(
                            "revanced_alt_thumbnail_fast_quality_summary_on",
                            "Using medium quality stills. Thumbnails will load faster, but live streams, unreleased, or very old videos may show blank thumbnails"
                        ),
                        StringResource("revanced_alt_thumbnail_fast_quality_summary_off", "Using high quality stills")
                    ),
                    NonInteractivePreference(
                        StringResource("revanced_alt_thumbnail_about_title", "About"),
                        StringResource(
                            "revanced_alt_thumbnail_about_summary",
                            "Alternative thumbnails are still images from the beginning/middle/end of each video. No external API is used, as these images are built into YouTube"
                        )
                    )
                ),
                StringResource("revanced_alt_thumbnails_preference_screen_summary", "Video thumbnail settings")
            )
        )

        MessageDigestImageUrlParentFingerprint.result
            ?: throw MessageDigestImageUrlParentFingerprint.exception
        MessageDigestImageUrlFingerprint.resolve(context, MessageDigestImageUrlParentFingerprint.result!!.classDef)
        MessageDigestImageUrlFingerprint.result?.apply {
            loadImageUrlMethod = mutableMethod
        } ?: throw MessageDigestImageUrlFingerprint.exception
        addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR, true)


        CronetURLRequestCallbackOnResponseStartedFingerprint.result
            ?: throw CronetURLRequestCallbackOnResponseStartedFingerprint.exception
        CronetURLRequestCallbackOnSucceededFingerprint.resolve(
            context,
            CronetURLRequestCallbackOnResponseStartedFingerprint.result!!.classDef
        )
        CronetURLRequestCallbackOnSucceededFingerprint.result?.apply {
            loadImageSuccessCallbackMethod = mutableMethod
        } ?: throw CronetURLRequestCallbackOnSucceededFingerprint.exception
        addImageUrlSuccessCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)


        CronetURLRequestCallbackOnFailureFingerprint.resolve(
            context,
            CronetURLRequestCallbackOnResponseStartedFingerprint.result!!.classDef
        )
        CronetURLRequestCallbackOnFailureFingerprint.result?.apply {
            loadImageErrorCallbackMethod = mutableMethod
        } ?: throw CronetURLRequestCallbackOnFailureFingerprint.exception
    }
}
