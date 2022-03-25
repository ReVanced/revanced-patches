package fi.razerman.youtube.Helpers;

import android.text.TextUtils;

/* loaded from: classes6.dex */
public class NullCheck {
    public static String ensureHasFragment(String fragmentName) {
        return TextUtils.isEmpty(fragmentName) ? "placeholder" : fragmentName;
    }
}
