package app.grapheneos.gmscompat.lib.playintegrity;

import android.annotation.SuppressLint;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.os.FakeBackgroundHandler;
import com.google.android.play.core.integrity.protocol.IExpressIntegrityService;
import com.google.android.play.core.integrity.protocol.IExpressIntegrityServiceCallback;

@SuppressLint("LongLogTag")
class StandardPlayIntegrityServiceWrapper extends PlayIntegrityServiceWrapper {

    StandardPlayIntegrityServiceWrapper(IBinder base) {
        super(base);
        requestIntegrityTokenTxnCode = 3; // IExpressIntegrityService.Stub.TRANSACTION_requestIntegrityToken
    }

    static class TokenRequestStub extends IExpressIntegrityService.Stub {
        public void requestIntegrityToken(Bundle request, IExpressIntegrityServiceCallback callback) {
            Runnable r = () -> {
                var result = new Bundle();
                // https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/StandardIntegrityErrorCode.html#API_NOT_AVAILABLE
                final int API_NOT_AVAILABLE = -1;
                result.putInt("error", API_NOT_AVAILABLE);
                try {
                    callback.onRequestExpressIntegrityTokenResult(result);
                } catch (RemoteException e) {
                    Log.e("IExpressIntegrityService.Stub", "", e);
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
