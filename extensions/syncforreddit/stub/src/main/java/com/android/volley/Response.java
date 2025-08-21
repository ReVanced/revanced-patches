package com.android.volley;

public class Response<T> {
    public interface Listener<T> {
        void onResponse(T t);
    }
}
