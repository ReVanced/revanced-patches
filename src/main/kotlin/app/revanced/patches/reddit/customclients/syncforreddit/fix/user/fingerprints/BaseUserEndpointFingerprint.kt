package app.revanced.patches.reddit.customclients.syncforreddit.fix.user.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal abstract class BaseUserEndpointFingerprint(source: String, accessFlags: Int? = null) :
    MethodFingerprint(
        accessFlags = accessFlags,
        strings = listOf("u/"),
        customFingerprint = { _, classDef -> classDef.sourceFile == source },
    )
