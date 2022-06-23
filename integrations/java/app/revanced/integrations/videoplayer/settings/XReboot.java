package app.revanced.integrations.videoplayer.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import app.revanced.integrations.settings.Settings;
import app.revanced.integrations.settings.XSettingsFragment;

/* loaded from: classes6.dex */
public class XReboot {

    /**
     * @param homeActivityClass Shell_HomeActivity.class
     */
    static void Reboot(Activity activity, Class homeActivityClass) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, homeActivityClass), intent));
        Process.killProcess(Process.myPid());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void RebootDialog(final Activity activity) {
        // from class: app.revanced.integrations.videoplayer.settings.XReboot.1
        // android.content.DialogInterface.OnClickListenerXSettingsFragment.homeActivityClass
        new AlertDialog.Builder(activity).setMessage(Settings.getStringByName(activity, "pref_refresh_config")).setPositiveButton(Settings.getStringByName(activity, "in_app_update_restart_button"), (dialog, id) -> XReboot.Reboot(activity, XSettingsFragment.homeActivityClass)).setNegativeButton(Settings.getStringByName(activity, "sign_in_cancel"), null).show();
    }
}
