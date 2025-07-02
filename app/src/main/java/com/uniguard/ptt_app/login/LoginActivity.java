package com.uniguard.ptt_app.login;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import com.uniguard.humla.model.Server;
import com.uniguard.ptt_app.Constants;
import com.uniguard.ptt_app.R;
import com.uniguard.ptt_app.app.MumlaActivity;
import com.uniguard.ptt_app.data.models.response.DefaultResponse;
import com.uniguard.ptt_app.data.models.response.LoginResponse;
import com.uniguard.ptt_app.data.models.ServerApi;
import com.uniguard.ptt_app.data.models.request.LoginRequest;
import com.uniguard.ptt_app.db.DatabaseCertificate;
import com.uniguard.ptt_app.db.MumlaDatabase;
import com.uniguard.ptt_app.db.MumlaSQLiteDatabase;
import com.uniguard.ptt_app.repository.UserRepository;
import com.uniguard.ptt_app.util.DownloadCertificateTask;
import com.uniguard.ptt_app.util.PreferenceLocationHelper;
import com.uniguard.ptt_app.util.ProgressDialogUtil;
import com.uniguard.ptt_app.util.SaveCertificateDownload;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private ProgressDialogUtil progressDialogUtil;
    private UserRepository userRepository;
    private SharedPreferences preferences;
    private MumlaDatabase mDatabase;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getDeviceLocation();

        mDatabase = new MumlaSQLiteDatabase(this);
        mDatabase.open();

        usernameEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);

        progressDialogUtil = new ProgressDialogUtil(this);


        userRepository = new UserRepository();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        loginButton.setOnClickListener(v -> {
            Log.d("LOGIN", "Clicked");

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty()) {
                usernameEditText.setError("Username is required");
                usernameEditText.requestFocus();
            } else if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
            } else {
                progressDialogUtil.show();
                doLogin(new LoginRequest(username, password));
            }
        });

    }


    private void getDeviceLocation() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), REQUEST_CODE);
            return;
        }

        // If permissions are granted, request location updates
        fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null && locationResult.getLocations() != null) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    PreferenceLocationHelper.saveLocation(LoginActivity.this, latitude, longitude);
                } else {
                    Toast.makeText(LoginActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    private void doLogin(LoginRequest request) {
        userRepository.login(request, new UserRepository.LoginCallBack() {
            @Override
            public void onSuccess(DefaultResponse<LoginResponse> response) {
                progressDialogUtil.dismiss();
                Toast.makeText(getApplicationContext(), response.getMessage(), Toast.LENGTH_LONG).show();
                getDeviceLocation();
                preferences.edit()
                        .putBoolean(Constants.PREF_IS_LOGIN, true)
                        .putString(Constants.PREF_TOKEN, response.getData().getAccessToken())
                        .putString(Constants.PREF_REFRESH_TOKEN, response.getData().getRefreshToken())
                        .putString(Constants.PREF_NAME, response.getData().getName())
                        .apply();

                for (DatabaseCertificate certificate : mDatabase.getCertificates()) {
                    mDatabase.removeCertificate(certificate.getId());
                }

                String certificate = response.getData().getCertificatePath();
                String name = response.getData().getName();
                String fileName = name.replace(" ", "-") + ".p12";

                if (certificate != null) {
                    String downloadUrl = Constants.STORAGE_URL + response.getData().getCertificatePath();
                    new DownloadCertificateTask(LoginActivity.this, fileName, new DownloadCertificateTask.DownloadListener() {
                        @Override
                        public void onDownloadComplete(String filePath) {
                            new SaveCertificateDownload(LoginActivity.this, filePath).save();
                        }

                        @Override
                        public void onDownloadFailed() {
                            Toast.makeText(LoginActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                        }
                    }).execute(downloadUrl);
                }

                List<Server> servers = mDatabase.getServers();
                for (Server server : servers) {
                    Log.d("LoginAct", "onSuccess: removed prev servers");
                    mDatabase.removeServer(server);
                }

                Log.d("LoginAct", "onSuccess: server size: " + response.getData().getServers().size());

                for (ServerApi serverApi : response.getData().getServers()) {
                    if (validate(serverApi)) {
                        Server server = new Server(serverApi.getServer().getId(), serverApi.getServer().getName(), serverApi.getServer().getHost(), serverApi.getServer().getPort(), response.getData().getEmail(), serverApi.getServer().getPassword());
                        mDatabase.addServer(server);
                        Log.d("LoginAct", "onSuccess: server added");
                    }
                }

                Intent intent = new Intent(LoginActivity.this, MumlaActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Throwable r) {
                progressDialogUtil.dismiss();
                Toast.makeText(getApplicationContext(), r.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mDatabase.close();
        super.onDestroy();
    }

    public boolean validate(ServerApi serverApi) {
        String valueOf = String.valueOf(serverApi.getServer().getPort());
        if (serverApi.getServer().getHost().length() == 0) {
            return false;
        }
        if (valueOf.length() > 0) {
            try {
                int parseInt = Integer.parseInt(valueOf);
                if (parseInt < 1 || parseInt > 65535) {
                    return false;
                }
            } catch (NumberFormatException unused) {
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
