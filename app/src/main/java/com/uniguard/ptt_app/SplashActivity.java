package com.uniguard.ptt_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import com.uniguard.ptt_app.app.MumlaActivity;
import com.uniguard.ptt_app.data.models.ServerApi;
import com.uniguard.ptt_app.login.LoginActivity;
import com.uniguard.ptt_app.repository.ServerRepository;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_SCREEN_DELAY = 2000; // 2 seconds
    private long startTime;
    private SharedPreferences preferences;
    private ServerRepository serverRepository;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        serverRepository = new ServerRepository();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        startTime = System.currentTimeMillis();
        checkTokenAndNavigate();


    }

    private void checkTokenAndNavigate() {
        String token = preferences.getString(Constants.PREF_TOKEN, null);

        if (token != null) {
            getServers(token);
        } else {
            // Token doesn't exist, navigate to LoginActivity
            intent = new Intent(SplashActivity.this, LoginActivity.class);
            navigateToNextActivity();
        }
    }

    private void navigateToNextActivity() {
        // Calculate the remaining time to reach the minimum splash screen time
        long elapsedTime = System.currentTimeMillis() - startTime;
        long remainingTime = SPLASH_SCREEN_DELAY - elapsedTime;

        if (remainingTime > 0) {
            // Delay the navigation to ensure splash screen is visible for at least 2 seconds
            new Handler().postDelayed(() -> {
                startActivity(intent);
                finish();
            }, remainingTime);
        } else {
            // If the elapsed time is already more than the minimum delay, navigate immediately
            startActivity(intent);
            finish();
        }
    }

    private void getServers(String token) {
        serverRepository.getServers(token, new ServerRepository.GetServerCallback() {
            @Override
            public void onSuccess(List<ServerApi> response) {
                intent = new Intent(SplashActivity.this, MumlaActivity.class);
                navigateToNextActivity();
            }

            @Override
            public void onError(Throwable t) {
                preferences.edit()
                        .putBoolean(Constants.PREF_IS_LOGIN, false)
                        .putString(Constants.PREF_TOKEN, null)
                        .putString(Constants.PREF_REFRESH_TOKEN, null)
                        .apply();

                intent = new Intent(SplashActivity.this, LoginActivity.class);
                navigateToNextActivity();
            }
        });
    }
}
