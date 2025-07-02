package com.uniguard.ptt_app.repository;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.uniguard.ptt_app.data.APIService;
import com.uniguard.ptt_app.data.models.response.ActivityResponse;
import com.uniguard.ptt_app.data.models.response.DefaultResponse;
import com.uniguard.ptt_app.data.models.response.LoginResponse;
import com.uniguard.ptt_app.data.models.response.LogoutResponse;
import com.uniguard.ptt_app.data.models.RefreshToken;
import com.uniguard.ptt_app.data.models.request.LoginRequest;
import com.uniguard.ptt_app.network.RetrofitClient;
import com.uniguard.ptt_app.util.ApiUtils;

public class UserRepository {
    private final APIService apiService;

    public UserRepository() {
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(APIService.class);
    }

    public void login(LoginRequest loginRequest, final LoginCallBack loginCallBack) {
        this.apiService.login(loginRequest.getEmail(), loginRequest.getPassword()).enqueue(new Callback<DefaultResponse<LoginResponse>>() {
            public void onResponse(Call<DefaultResponse<LoginResponse>> call, Response<DefaultResponse<LoginResponse>> response) {
                if (response.isSuccessful()) {
                    loginCallBack.onSuccess(response.body());
                    return;
                }
                loginCallBack.onError(new Exception(ApiUtils.convertErrorMessage(response)));
            }

            public void onFailure(Call<DefaultResponse<LoginResponse>> call, Throwable th) {
                loginCallBack.onError(th);
            }
        });
    }

    public void refreshToken(String str, final RefreshTokenCallback refreshTokenCallback) {
        this.apiService.refreshToken(str).enqueue(new Callback<DefaultResponse<RefreshToken>>() {
            public void onResponse(Call<DefaultResponse<RefreshToken>> call, Response<DefaultResponse<RefreshToken>> response) {
                if (response.isSuccessful()) {
                    refreshTokenCallback.onSuccess(response.body());
                    return;
                }
                refreshTokenCallback.onError(new Exception(ApiUtils.convertErrorMessage(response)));
            }

            public void onFailure(Call<DefaultResponse<RefreshToken>> call, Throwable th) {
                refreshTokenCallback.onError(th);
            }
        });
    }

    public void logout(String str, final LogoutCallback logoutCallback) {
        APIService apiService = this.apiService;
        apiService.logout("Bearer " + str).enqueue(new Callback<LogoutResponse>() {
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                if (response.isSuccessful()) {
                    logoutCallback.onSuccess(response.body());
                    return;
                }
                logoutCallback.onError(new Exception(ApiUtils.convertErrorMessage(response)));
            }

            public void onFailure(Call<LogoutResponse> call, Throwable th) {
                logoutCallback.onError(th);
            }
        });
    }

    public void postActivity(String token, String activity, File file, ActivityCallback callback) {

        // Buat RequestBody untuk data aktivitas
        RequestBody activityPart = RequestBody.create(activity, MediaType.parse("multipart/form-data"));

        // Buat RequestBody dan MultipartBody.Part untuk file lampiran
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("attachment", file.getName(), requestFile);


        APIService apiService = this.apiService;
        apiService.postActivity("Bearer " + token, activityPart, filePart).enqueue(new Callback<ActivityResponse>() {
            @Override
            public void onResponse(Call<ActivityResponse> call, Response<ActivityResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    return;
                }
                callback.onError(new Exception(ApiUtils.convertErrorMessage(response)));
            }

            @Override
            public void onFailure(Call<ActivityResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }


    public interface LoginCallBack {
        void onSuccess(DefaultResponse<LoginResponse> response);

        void onError(Throwable r);
    }


    public interface RefreshTokenCallback {
        void onSuccess(DefaultResponse<RefreshToken> response);

        void onError(Throwable r);
    }

    public interface LogoutCallback {
        void onSuccess(LogoutResponse response);

        void onError(Throwable r);
    }

    public interface ActivityCallback {
        void onSuccess(ActivityResponse response);

        void onError(Throwable r);
    }
}
