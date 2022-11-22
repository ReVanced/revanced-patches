package app.revanced.patches.tiktok.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object AwemeGetVideoControlFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC.value,
   customFingerprint = { methodDef ->
       methodDef.definingClass.endsWith("/Aweme;") && methodDef.name == "getVideoControl"
   }
)