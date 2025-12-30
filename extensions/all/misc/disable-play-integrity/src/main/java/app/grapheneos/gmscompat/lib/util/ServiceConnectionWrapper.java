package app.grapheneos.gmscompat.lib.util;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import java.util.function.UnaryOperator;

public class ServiceConnectionWrapper implements ServiceConnection {
    private final ServiceConnection base;
    private final UnaryOperator<IBinder> binderOverride;

    public ServiceConnectionWrapper(ServiceConnection base, UnaryOperator<IBinder> binderOverride) {
        this.base = base;
        this.binderOverride = binderOverride;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IBinder override = binderOverride.apply(service);
            if (override != null) {
                service = override;
            }
        }

        base.onServiceConnected(name, service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        base.onServiceDisconnected(name);
    }

    @Override
    public void onBindingDied(ComponentName name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            base.onBindingDied(name);
        }
    }

    @Override
    public void onNullBinding(ComponentName name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            base.onNullBinding(name);
        }
    }
}
