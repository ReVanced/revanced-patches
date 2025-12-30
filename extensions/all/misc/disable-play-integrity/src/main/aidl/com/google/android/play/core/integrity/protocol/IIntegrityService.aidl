package com.google.android.play.core.integrity.protocol;

import android.os.Bundle;
import com.google.android.play.core.integrity.protocol.IIntegrityServiceCallback;

interface IIntegrityService {
    oneway void requestIntegrityToken(in Bundle request, IIntegrityServiceCallback callback) = 1;
}
