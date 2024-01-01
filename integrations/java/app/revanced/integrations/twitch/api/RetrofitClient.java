package app.revanced.integrations.twitch.api;

import retrofit2.Retrofit;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private final PurpleAdblockApi purpleAdblockApi;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://localhost" /* dummy */).build();
        purpleAdblockApi = retrofit.create(PurpleAdblockApi.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public PurpleAdblockApi getPurpleAdblockApi() {
        return purpleAdblockApi;
    }
}