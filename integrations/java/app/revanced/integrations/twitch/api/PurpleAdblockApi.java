package app.revanced.integrations.twitch.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/* only used for service pings */
public interface PurpleAdblockApi {
    @GET /* root */
    Call<ResponseBody> ping(@Url String baseUrl);
}