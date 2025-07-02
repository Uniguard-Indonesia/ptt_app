package com.uniguard.ptt_app.repository;

import androidx.annotation.NonNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.uniguard.ptt_app.data.APIService;
import com.uniguard.ptt_app.data.models.ServerApi;
import com.uniguard.ptt_app.network.RetrofitClient;
import com.uniguard.ptt_app.util.ApiUtils;

public class ServerRepository {
    private final APIService apiService;

    public ServerRepository(){
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(APIService.class);
    }

    public void getServers(String token, final GetServerCallback callback){
        Call<List<ServerApi>> call = apiService.getServers("Bearer "+token);
        call.enqueue(new Callback<List<ServerApi>>() {
            @Override
            public void onResponse(@NonNull Call<List<ServerApi>> call, @NonNull Response<List<ServerApi>> response) {
                if(response.isSuccessful()){
                    callback.onSuccess(response.body());
                }else{
                    callback.onError(new Exception(ApiUtils.convertErrorMessage(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ServerApi>> call, @NonNull Throwable t) {
                callback.onError(t);
            }
        });
    }

    public interface GetServerCallback{
        void onSuccess(List<ServerApi> response);
        void onError(Throwable t);
    }
}
