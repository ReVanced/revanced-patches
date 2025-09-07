package app.revanced.patches.reddit.customclients.baconreader.fix.redgifs

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal val getOkHttpClientFingerprint = fingerprint {
    returns("Lokhttp3/OkHttpClient;")
    parameters()
    custom { method, classDef ->
        classDef.type == "Lcom/onelouder/baconreader/media/gfycat/RedGifsManager;" && method.name == "getOkhttpClient"
    }
}
