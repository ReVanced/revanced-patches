package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility
import org.jf.dexlib2.util.MethodUtil

@Name("next-gen-watch-layout-fingerprint")

@SponsorBlockCompatibility
@Version("0.0.1")
object NextGenWatchLayoutFingerprint : MethodFingerprint(
    "V", // constructors return void, in favour of speed of matching, this fingerprint has been added
    customFingerprint =  { methodDef -> MethodUtil.isConstructor(methodDef) && methodDef.parameterTypes.size == 3 && methodDef.definingClass.endsWith("NextGenWatchLayout;") }
)