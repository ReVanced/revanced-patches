package com.google.android.play.core.integrity.protocol;

import android.os.Bundle;

interface IIntegrityServiceCallback {
    oneway void onResult(in Bundle result) = 1;
}
