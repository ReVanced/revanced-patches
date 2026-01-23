package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal val BytecodePatchContext.adsInsertionMethod by runMethod(
    fieldValue = $$"AdBucketDataSourceUtil$attemptAdsInsertion$1",
)

internal val BytecodePatchContext.fetchMoreAdsMethod by runMethod(
    fieldValue = $$"AdBucketDataSourceUtil$attemptFetchMoreAds$1",
)

internal fun runMethod(fieldValue: String) = gettingFirstMutableMethodDeclaratively {
    name("run")
    returnType("V")
    parameterTypes()
    custom {
        immutableClassDef.anyField {
            name == "__redex_internal_original_name" && (initialValue as? StringEncodedValue)?.value == fieldValue
        }
    }
}
