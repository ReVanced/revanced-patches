package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.util.MethodUtil

object NextGenWatchLayoutFingerprint : MethodFingerprint(
    "V", // constructors return void, in favour of speed of matching, this fingerprint has been added
    customFingerprint =  { methodDef -> MethodUtil.isConstructor(methodDef) && methodDef.parameterTypes.size == 3 && methodDef.definingClass.endsWith("NextGenWatchLayout;") }
)