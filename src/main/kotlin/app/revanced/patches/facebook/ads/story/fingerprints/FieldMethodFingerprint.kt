package app.revanced.patches.facebook.ads.story.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal fun fieldMethodFingerprint(fieldValue: String) = methodFingerprint {
    returns("V")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "run" && classDef.fields.any any@{ field ->
            if (field.name != "__redex_internal_original_name") return@any false
            (field.initialValue as? StringEncodedValue)?.value == fieldValue
        }
    }
}
