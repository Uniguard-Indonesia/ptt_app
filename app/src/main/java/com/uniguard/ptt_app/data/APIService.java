package com.uniguard.ptt_app.data;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import com.uniguard.ptt_app.data.models.RefreshToken;
import com.uniguard.ptt_app.data.models.ServerApi;
import com.uniguard.ptt_app.data.models.User;
import com.uniguard.ptt_app.data.models.response.ActivityResponse;
import com.uniguard.ptt_app.data.models.response.DefaultListResponse;
import com.uniguard.ptt_app.data.models.response.DefaultResponse;
import com.uniguard.ptt_app.data.models.response.LoginResponse;
import com.uniguard.ptt_app.data.models.response.LogoutResponse;
import com.uniguard.ptt_app.data.models.response.PositionResponse;

public interface APIService {
    @GET("position")
    Call<DefaultListResponse<User>> getPositions(@Header("Authorization") String str);

    @GET("servers")
    Call<List<ServerApi>> getServers(@Header("Authorization") String str);

    @POST("auth/login")
    Call<DefaultResponse<LoginResponse>> login(@Query("email") String str, @Query("password") String str2);

    @POST("auth/logout")
    Call<LogoutResponse> logout(@Header("Authorization") String str);

    @POST("auth/refresh-token")
    Call<DefaultResponse<RefreshToken>> refreshToken(@Field("refresh_token") String str);

    @POST("position")
    Call<PositionResponse> updatePosition(@Header("Authorization") String str, @Query("latitude") String str2, @Query("longitude") String str3);

    @POST("activity")
    @Multipart
    Call<ActivityResponse> postActivity(
            @Header("Authorization") String token,
            @Part("activity") RequestBody activity,
            @Part MultipartBody.Part attachment
    );
}
