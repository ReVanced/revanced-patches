package app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint

internal object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/laurencedawson/reddit_sync/RedditApplication;" && methodDef.name == "onCreate" },
    insertIndexResolver = { 1 } // Insert after call to super class.
)