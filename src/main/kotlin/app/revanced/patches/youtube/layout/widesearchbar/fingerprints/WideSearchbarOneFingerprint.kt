package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("wide-searchbar-methodone-fingerprint")
@MatchingMethod(
    "Ljkg;", "i"
)
@DirectPatternScanMethod
@WideSearchbarCompatibility
@Version("0.0.1")

/*
This finds the following method:
public final View i(akrl akrlVar, adpd adpdVar) {
}

Method we search for is located in smali now.
See:
.method public final i(Lakrl;Ladpd;)Landroid/view/View;
    iget-object v0, p0, Ljkg;->a:Landroid/app/Activity;
    invoke-static {v0}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;
    move-result-object v0
    iget-object v1, p0, Ljkg;->g:Ljis;
    const/4 v2, 0x0
    invoke-interface {v1, v2}, Ljis;->h(Z)V
    iget-object p1, p1, Lakrl;->f:Lahnq;
    if-nez p1, :cond_12
    sget-object p1, Lahnq;->a:Lahnq;
    :cond_12
    iget-object v1, p0, Ljkg;->x:Ltxm;
    invoke-static {v1}, Lfbn;->aF(Ltxm;)Z //THIS IS WHAT WE SEARCH FOR (Method fbn.aF)
    move-result v1
 */

object WideSearchbarOneFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L", "L"), listOf(Opcode.IF_NEZ, Opcode.SGET_OBJECT, Opcode.IGET_OBJECT, Opcode.INVOKE_STATIC),
    null, null
)