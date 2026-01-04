package app.grapheneos.gmscompat.lib.playintegrity;

import android.os.Binder;
import android.os.BinderWrapper;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

abstract class PlayIntegrityServiceWrapper extends BinderWrapper {
    final String TAG;
    protected int requestIntegrityTokenTxnCode;

    public PlayIntegrityServiceWrapper(IBinder base) {
        super(base);
        TAG = getClass().getSimpleName();
    }

    protected abstract Binder createTokenRequestStub();

    @Override
    public boolean transact(int code, Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (code == requestIntegrityTokenTxnCode) {
            if (maybeStubOutIntegrityTokenRequest(code, data, reply, flags)) {
                return true;
            }
        }
        return super.transact(code, data, reply, flags);
    }

    private boolean maybeStubOutIntegrityTokenRequest(int code, Parcel data, @Nullable Parcel reply, int flags) {
        Log.d(TAG, "integrity token request detected");

        try {
            createTokenRequestStub().transact(code, data, reply, flags);
        } catch (RemoteException e) {
            // this is a local call
            throw new IllegalStateException(e);
        }
        return true;
    }

    protected static long getTokenRequestResultDelay() {
        return 500L;
    }
}
