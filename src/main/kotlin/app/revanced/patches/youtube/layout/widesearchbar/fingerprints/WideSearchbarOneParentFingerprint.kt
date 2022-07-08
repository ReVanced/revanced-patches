package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import org.jf.dexlib2.AccessFlags

@Name("wide-searchbar-methodone-parent-fingerprint")
@MatchingMethod(
    "Ljkg;", "l"
)
@FuzzyPatternScanMethod(3)
@WideSearchbarCompatibility
@Version("0.0.1")

/*
 * This finds the following method:
  private final void l(PaneDescriptor paneDescriptor) {
        Class cls = null;
        ahat f = paneDescriptor != null ? paneDescriptor.f() : null;
        if (paneDescriptor != null) {
            cls = paneDescriptor.a;
        }
        ftu k = k();
        if ((k == null || !k.bk()) && ((!frx.k(f) && !((Boolean) aqer.S(this.n).X(new fac(this, f, 19)).K(irx.i).aH(false)).booleanValue()) || (f != null && f.qA(ReelWatchEndpointOuterClass$ReelWatchEndpoint.reelWatchEndpoint)))) {
            String j = frx.j(f);
            if ((j != null && ("FEhistory".equals(j) || "FEmy_videos".equals(j) || "FEpurchases".equals(j) || j.startsWith("VL"))) || cls == this.I.E() || cls == this.G.a) {
                this.F = 3;
                return;
            } else {
                this.F = 2;
                return;
            }
        }
        this.F = 1;
    }
 */

object WideSearchbarOneParentFingerprint : MethodFingerprint(
    "V", AccessFlags.PRIVATE or AccessFlags.FINAL, listOf("L"), null,
    listOf("FEhistory", "FEmy_videos", "FEpurchases")
)