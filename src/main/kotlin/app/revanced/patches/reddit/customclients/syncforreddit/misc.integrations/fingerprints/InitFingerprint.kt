package app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint

internal object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.type == "Lcom/laurencedawson/reddit_sync/RedditApplication;"
    },
    insertIndexResolver = { 1 }, // Insert after call to super class.
)
