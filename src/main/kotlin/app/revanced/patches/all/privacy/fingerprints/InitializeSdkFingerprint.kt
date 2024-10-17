package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Matches com.moengage.core.internal.initialisation.initialiseSdk.
internal object InitializeSdkFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("moEngage", "App-Id is empty, SDK cannot be initialised."),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "InitialisationHandler.kt"
    },
)
