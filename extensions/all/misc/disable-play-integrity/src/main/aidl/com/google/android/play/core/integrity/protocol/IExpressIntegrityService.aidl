package com.google.android.play.core.integrity.protocol;

import android.os.Bundle;
import com.google.android.play.core.integrity.protocol.IExpressIntegrityServiceCallback;

interface IExpressIntegrityService {
    oneway void requestIntegrityToken(in Bundle request, IExpressIntegrityServiceCallback callback) = 2;
}
