package app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/rubenmayayo/reddit/MyApplication;" && methodDef.name == "onCreate" },
    insertIndexResolver = { 1 } // Insert after call to super class.
)