package cn.henu.typechatv2.network;

import retrofit2.Retrofit;

public class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/fcm/")
                    .build();
        }
        return retrofit;
    }
}
