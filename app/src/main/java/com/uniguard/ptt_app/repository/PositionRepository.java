package com.uniguard.ptt_app.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.uniguard.ptt_app.data.APIService;
import com.uniguard.ptt_app.data.models.User;
import com.uniguard.ptt_app.data.models.request.PositionRequest;
import com.uniguard.ptt_app.data.models.response.DefaultListResponse;
import com.uniguard.ptt_app.data.models.response.PositionResponse;
import com.uniguard.ptt_app.network.RetrofitClient;
import com.uniguard.ptt_app.util.ApiUtils;

public class PositionRepository {

    private final APIService apiService;

    public PositionRepository() {
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(APIService.class);
    }

    public void getPositions(String str, final GetPositionsCallback getPositionsCallback) {
        APIService aPIService = this.apiService;
        aPIService.getPositions("Bearer " + str).enqueue(new Callback<DefaultListResponse<User>>() {
            public void onResponse(Call<DefaultListResponse<User>> call, Response<DefaultListResponse<User>> response) {
                if (response.isSuccessful()) {
                    getPositionsCallback.onSuccess(response.body());
                } else {
                    getPositionsCallback.onError(new Exception(ApiUtils.convertErrorMessage(response)));
                }
            }

            public void onFailure(Call<DefaultListResponse<User>> call, Throwable th) {
                getPositionsCallback.onError(th);
            }
        });
    }

    public void updatePosition(String str, PositionRequest positionRequest, final UpdatePositionCallback updatePositionCallback) {
        APIService apiService = this.apiService;
        apiService.updatePosition("Bearer " + str, positionRequest.getLatitude(), positionRequest.getLongitude()).enqueue(new Callback<PositionResponse>() {
            public void onResponse(Call<PositionResponse> call, Response<PositionResponse> response) {
                if (response.isSuccessful()) {
                    updatePositionCallback.onSuccess(response.body());
                } else {
                    updatePositionCallback.onError(new Exception(ApiUtils.convertErrorMessage(response)));
                }
            }

            public void onFailure(Call<PositionResponse> call, Throwable th) {
                updatePositionCallback.onError(th);
            }
        });
    }


    public interface GetPositionsCallback{
        void onSuccess(DefaultListResponse<User> response);
        void onError(Throwable t);
    }

    public interface UpdatePositionCallback{
        void onSuccess(PositionResponse response);
        void onError(Throwable t);
    }
}
