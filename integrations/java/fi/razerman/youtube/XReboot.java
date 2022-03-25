package fi.razerman.youtube;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import com.google.android.apps.youtube.app.application.Shell_HomeActivity;

/* loaded from: classes6.dex */
public class XReboot {
    static void Reboot(Activity activity) {
        int intent;
        intent = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        ((AlarmManager) activity.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.ELAPSED_REALTIME, 1500L, PendingIntent.getActivity(activity, 0, new Intent(activity, Shell_HomeActivity.class), intent));
        Process.killProcess(Process.myPid());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void RebootDialog(final Activity activity) {
        // from class: fi.razerman.youtube.XReboot.1
// android.content.DialogInterface.OnClickListener
        new AlertDialog.Builder(activity).setMessage(XGlobals.getStringByName(activity, "pref_refresh_config")).setPositiveButton(XGlobals.getStringByName(activity, "in_app_update_restart_button"), (dialog, id) -> XReboot.Reboot(activity)).setNegativeButton(XGlobals.getStringByName(activity, "sign_in_cancel"), null).show();
    }
}
