package app.revanced.patches.twitch.ad.embedded

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.twitch.ad.embedded.fingerprints.CreateUsherClientFingerprint
import app.revanced.patches.twitch.ad.video.VideoAdsPatch
import app.revanced.patches.twitch.misc.integrations.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.SettingsPatch

@Patch(
    name = "Block embedded ads",
    description = "Blocks embedded stream ads using services like Luminous or PurpleAdBlocker.",
    dependencies = [VideoAdsPatch::class, IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("tv.twitch.android.app", ["15.4.1", "16.1.0"])]
)
@Suppress("unused")
object EmbeddedAdsPatch : BytecodePatch(
    setOf(CreateUsherClientFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val result = CreateUsherClientFingerprint.result ?: throw CreateUsherClientFingerprint.exception

        // Inject OkHttp3 application interceptor
        result.mutableMethod.addInstructions(
            3,
            """
                invoke-static  {}, Lapp/revanced/twitch/patches/EmbeddedAdsPatch;->createRequestInterceptor()Lapp/revanced/twitch/api/RequestInterceptor;
                move-result-object v2
                invoke-virtual {v0, v2}, Lokhttp3/OkHttpClient${"$"}Builder;->addInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient${"$"}Builder;
            """
        )

        SettingsPatch.PreferenceScreen.ADS.SURESTREAM.addPreferences(
            ListPreference(
                "revanced_block_embedded_ads",
                StringResource(
                    "revanced_block_embedded_ads",
                    "Block embedded video ads"
                ),
                ArrayResource(
                    "revanced_hls_proxies",
                    listOf(
                        StringResource("revanced_proxy_disabled", "Disabled"),
                        StringResource("revanced_proxy_luminous", "Luminous proxy"),
                        StringResource("revanced_proxy_purpleadblock", "PurpleAdBlock proxy"),
                    )
                ),
                ArrayResource(
                    "revanced_hls_proxies_values",
                    listOf(
                        StringResource("key_revanced_proxy_disabled", "disabled"),
                        StringResource("key_revanced_proxy_luminous", "luminous"),
                        StringResource("key_revanced_proxy_purpleadblock", "purpleadblock")
                    )
                ),
                default = "luminous"
            )
        )

        SettingsPatch.addString("revanced_embedded_ads_service_unavailable", "%s is unavailable. Ads may show. Try switching to another ad block service in settings.")
        SettingsPatch.addString("revanced_embedded_ads_service_failed", "%s server returned an error. Ads may show. Try switching to another ad block service in settings.")
    }
}
