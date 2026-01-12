package com.google.android.play.core.integrity.protocol;

interface IExpressIntegrityServiceCallback {
    oneway void onRequestExpressIntegrityTokenResult(in Bundle result) = 2;
}
