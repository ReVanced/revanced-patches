package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal val adsInsertionFingerprint = fieldFingerprint(
    fieldValue = "AdBucketDataSourceUtil\$attemptAdsInsertion\$1",
)

internal val fetchMoreAdsFingerprint = fieldFingerprint(
    fieldValue = "AdBucketDataSourceUtil\$attemptFetchMoreAds\$1",
)

internal fun fieldFingerprint(fieldValue: String) = fingerprint {
    returns("V")
    parameters()
    custom { method, classDef ->
        method.name == "run" &&
            classDef.fields.any any@{ field ->
                if (field.name != "__redex_internal_original_name") return@any false
                (field.initialValue as? StringEncodedValue)?.value == fieldValue
            }
    }
}
