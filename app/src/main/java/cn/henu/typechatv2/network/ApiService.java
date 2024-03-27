package cn.henu.typechatv2.network;

import java.util.HashMap;

import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.Call;
public interface ApiService {

    @POST("send")
    Call<String> sendMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String body
    );
}
