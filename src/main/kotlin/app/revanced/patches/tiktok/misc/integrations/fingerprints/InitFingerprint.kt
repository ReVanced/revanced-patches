package app.revanced.patches.tiktok.misc.integrations.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object InitFingerprint : IntegrationsFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/AwemeHostApplication;") &&
                methodDef.name == "<init>"
    },
    insertIndexResolver = { 1 } // Insert after call to super class.
)