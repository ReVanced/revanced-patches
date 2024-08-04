package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal object PlayerResponseModelAudioOnlyPlaybackParentFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef ->
        // The String is a static final field and not set in a static initializer
        // and cannot use a typical fingerprint string declaration.
        // Instead check the field itself.
        val field = classDef.staticFields.firstOrNull() // Target class has only 1 static field.
        (field?.initialValue as? StringEncodedValue)?.value == "double_tap_skip_duration"
    }
)