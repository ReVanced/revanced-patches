package app.revanced.patches.youtube.misc.forcevp9.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.forcevp9.annotations.ForceVP9Compatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("force-vp9-codec-fingerprint")
@MatchingMethod(
    "Lpzs;", "aI"
)
@DirectPatternScanMethod
@ForceVP9Compatibility
@Version("0.0.1")
object ForceVP9CodecFingerprint : MethodFingerprint(
    "Z", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("L", "I"), listOf(
        Opcode.SGET, Opcode.IF_NEZ, Opcode.INVOKE_STATIC
    ), null, null
)

/*
public static boolean aI(Context context, int i) {
    if (b == 0) {
        aH(context);
    }
    return b >= i; // Override to: return Lapp/revanced/integrations/patches/ForceCodecPatch->shouldForceVP9()
}

.method public static aI(Landroid/content/Context;I)Z
    sget v0, Lpzs;->b:I
    if-nez v0, :cond_7
    invoke-static {p0}, Lpzs;->aH(Landroid/content/Context;)V
    :cond_7
    //remove after here, and inject only our code
    sget p0, Lpzs;->b:I
    if-lt p0, p1, :cond_d
    const/4 p0, 0x1
    return p0
    :cond_d
    const/4 p0, 0x0
    return p0
.end method
 */