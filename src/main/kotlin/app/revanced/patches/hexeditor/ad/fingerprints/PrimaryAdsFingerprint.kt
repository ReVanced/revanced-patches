package app.revanced.patches.hexeditor.ad.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.hexeditor.ad.annotations.HexEditorAdsCompatibility

@Name("ads-fingerprint-primary")
@HexEditorAdsCompatibility
@Version("0.0.1")
object PrimaryAdsFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("PreferencesHelper;") && methodDef.name == "isAdsDisabled"
    }
)