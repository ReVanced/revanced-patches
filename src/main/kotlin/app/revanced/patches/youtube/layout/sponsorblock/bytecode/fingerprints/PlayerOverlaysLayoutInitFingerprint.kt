package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerOverlaysLayoutInitFingerprint : MethodFingerprint(

    customFingerprint = { methodDef -> methodDef.returnType.endsWith("YouTubePlayerOverlaysLayout;") }
)