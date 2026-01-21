package app.grapheneos.gmscompat.lib.playintegrity;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.os.FakeBackgroundHandler;
import com.google.android.play.core.integrity.protocol.IIntegrityService;
import com.google.android.play.core.integrity.protocol.IIntegrityServiceCallback;

class ClassicPlayIntegrityServiceWrapper extends PlayIntegrityServiceWrapper {

    ClassicPlayIntegrityServiceWrapper(IBinder base) {
        super(base);
        requestIntegrityTokenTxnCode = 2; // IIntegrityService.Stub.TRANSACTION_requestIntegrityToken
    }

    static class TokenRequestStub extends IIntegrityService.Stub {
        public void requestIntegrityToken(Bundle request, IIntegrityServiceCallback callback) {
            Runnable r = () -> {
                var result = new Bundle();
                // https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityErrorCode.html#API_NOT_AVAILABLE
                final int API_NOT_AVAILABLE = -1;
                result.putInt("error", API_NOT_AVAILABLE);
                try {
                    callback.onResult(result);
                } catch (RemoteException e) {
                    Log.e("IIntegrityService.Stub", "", e);
                }
            };
            FakeBackgroundHandler.getHandler().postDelayed(r, getTokenRequestResultDelay());
        }
    };

    @Override
    protected Binder createTokenRequestStub() {
        return new TokenRequestStub();
    }
}
