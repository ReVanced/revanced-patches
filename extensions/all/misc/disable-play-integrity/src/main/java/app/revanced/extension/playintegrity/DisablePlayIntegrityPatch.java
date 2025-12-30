package app.revanced.extension.playintegrity;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import app.grapheneos.gmscompat.lib.playintegrity.PlayIntegrityUtils;

public class DisablePlayIntegrityPatch {
    public static boolean bindService(Context context, Intent service, ServiceConnection conn, int flags) {
        ServiceConnection override = PlayIntegrityUtils.maybeReplaceServiceConnection(service, conn);
        if (override != null) {
            conn = override;
        }

        return context.bindService(service, conn, flags);
    }
}
