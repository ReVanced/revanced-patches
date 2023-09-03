package app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints

object LoginActivityOnCreateFingerprint : AbstractClientIdFingerprint(custom@{ methodDef, classDef ->
    methodDef.name == "onCreate" && classDef.type.endsWith("LoginActivity;")
})