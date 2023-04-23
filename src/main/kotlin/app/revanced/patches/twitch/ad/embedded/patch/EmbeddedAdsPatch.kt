package app.revanced.patches.twitch.ad.embedded.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.twitch.ad.embedded.annotations.EmbeddedAdsCompatibility
import app.revanced.patches.twitch.ad.embedded.fingerprints.CreateUsherClientFingerprint
import app.revanced.patches.twitch.ad.video.patch.VideoAdsPatch
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([VideoAdsPatch::class, IntegrationsPatch::class, SettingsPatch::class])
@Name("block-embedded-ads")
@Description("Blocks embedded stream ads using services like TTV.lol or PurpleAdBlocker.")
@EmbeddedAdsCompatibility
@Version("0.0.1")
class EmbeddedAdsPatch : BytecodePatch(
    listOf(CreateUsherClientFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = CreateUsherClientFingerprint.result ?: return PatchResultError("${CreateUsherClientFingerprint.name} not found")

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
                        StringResource("revanced_proxy_ttv_lol", "TTV LOL proxy"),
                        StringResource("revanced_proxy_purpleadblock", "PurpleAdBlock proxy"),
                    )
                ),
                ArrayResource(
                    "revanced_hls_proxies_values",
                    listOf(
                        StringResource("key_revanced_proxy_disabled", "disabled"),
                        StringResource("key_revanced_proxy_ttv_lol", "ttv-lol"),
                        StringResource("key_revanced_proxy_purpleadblock", "purpleadblock")
                    )
                ),
                "ttv-lol"
            )
        )

        SettingsPatch.addString("revanced_embedded_ads_service_unavailable", "%s is unavailable. Ads may show. Try switching to another ad block service in settings.")
        SettingsPatch.addString("revanced_embedded_ads_service_failed", "%s server returned an error. Ads may show. Try switching to another ad block service in settings.")

        return PatchResultSuccess()
    }
}
