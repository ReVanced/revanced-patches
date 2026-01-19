package app.grapheneos.gmscompat.lib.playintegrity;

import android.content.Intent;
import android.content.ServiceConnection;
import android.ext.PackageId;
import android.os.IBinder;
import androidx.annotation.Nullable;
import app.grapheneos.gmscompat.lib.util.ServiceConnectionWrapper;
import java.util.function.UnaryOperator;

public class PlayIntegrityUtils {

    public static @Nullable ServiceConnection maybeReplaceServiceConnection(Intent service, ServiceConnection orig) {
        if (PackageId.PLAY_STORE_NAME.equals(service.getPackage())) {
            UnaryOperator<IBinder> binderOverride = null;

            final String CLASSIC_SERVICE =
                    "com.google.android.play.core.integrityservice.BIND_INTEGRITY_SERVICE";
            final String STANDARD_SERVICE =
                    "com.google.android.play.core.expressintegrityservice.BIND_EXPRESS_INTEGRITY_SERVICE";

            String action = service.getAction();
            if (STANDARD_SERVICE.equals(action)) {
                binderOverride = StandardPlayIntegrityServiceWrapper::new;
            } else if (CLASSIC_SERVICE.equals(action)) {
                binderOverride = ClassicPlayIntegrityServiceWrapper::new;
            }

            if (binderOverride != null) {
                return new ServiceConnectionWrapper(orig, binderOverride);
            }
        }
        return null;
    }
}
