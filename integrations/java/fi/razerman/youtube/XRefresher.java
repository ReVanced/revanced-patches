package fi.razerman.youtube;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;

/* loaded from: classes6.dex */
public final class XRefresher implements Preference.OnPreferenceClickListener {
    private final XSettingsFragment fragment;

    public XRefresher(XSettingsFragment xSettingsFragment) {
        this.fragment = xSettingsFragment;
    }

    @Override // android.preference.Preference.OnPreferenceClickListener
    public final boolean onPreferenceClick(Preference preference) {
        XSettingsFragment fragment = this.fragment;
        Handler handler = new Handler(Looper.getMainLooper());
        Activity activity = fragment.getActivity();
        activity.getClass();
        handler.postAtFrontOfQueue(new XRecreate(activity));
        return true;
    }
}
