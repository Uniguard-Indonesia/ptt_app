package com.uniguard.ptt_app.service.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.uniguard.ptt_app.Constants;
import com.uniguard.ptt_app.R;
import com.uniguard.ptt_app.data.models.request.PositionRequest;
import com.uniguard.ptt_app.data.models.response.PositionResponse;
import com.uniguard.ptt_app.repository.PositionRepository;
import com.uniguard.ptt_app.util.LocationUtils;
import com.uniguard.ptt_app.util.PreferenceLocationHelper;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final long TIMER_INTERVAL = 15 * 60 * 1000; // 1 minute
    private static final long WAKELOCK_TIMEOUT = 10 * 60 * 1000; // 10 minutes

    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private LocationCallback locationCallback;
    private Handler handler;
    private Runnable locationRequestRunnable;
    private PowerManager.WakeLock wakeLock;
    private PositionRepository positionRepository;

    @Override
    public void onCreate() {
        Log.d(TAG, "Service Created");

        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        positionRepository = new PositionRepository();
        createNotificationChannel();
        initLocationCallback();
        startForegroundServiceWithNotification();
        startLocationRequest();

        //  Initialize WakeLock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PTT::MyWakelockTag");
        wakeLock.acquire(WAKELOCK_TIMEOUT);

        Log.d(TAG, "WakeLock acquired");

        // Initialize SharedPreferences listener
        initPreferenceChangeListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start Command Location Service");
        if (this.preferences.getBoolean(Constants.PREF_IS_LOGIN, false)) {
            Log.d(TAG, "Service Started");
            startForegroundServiceWithNotification();
            startLocationRequest();
            return START_STICKY;
        }
        Log.d(TAG, "User is not logged in. Stopping the service.");
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");

        if (handler != null && locationRequestRunnable != null) {
            handler.removeCallbacks(locationRequestRunnable);
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        // Unregister SharedPreferences listener
        if (preferences != null && preferenceChangeListener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
    }

    private void startForegroundServiceWithNotification() {
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null){
                    return;
                }

                Location location = locationResult.getLastLocation();
                Log.d(LocationService.class.getName(), "lat: " + location.getLatitude() + " lon: " + location.getLongitude());

                handleLocation(location);
            }
        };
    }

    private void startLocationRequest() {
        handler = new Handler(Looper.getMainLooper());
        locationRequestRunnable = new Runnable() {
            @Override
            public void run() {
                @SuppressLint("VisibleForTests") LocationRequest locationRequest = LocationRequest.create()
                        .setInterval(TIMER_INTERVAL)
                        .setFastestInterval(TIMER_INTERVAL)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                handler.postDelayed(this, TIMER_INTERVAL);
            }
        };
        handler.post(locationRequestRunnable);
    }

    private void handleLocation(Location location) {
        boolean isLogin = preferences.getBoolean(Constants.PREF_IS_LOGIN, false);
        if (isLogin) {
            if (location != null) {
                if(LocationUtils.isMockLocation(location)){
                    Log.d(TAG, "handleLocation: Mock Location detected!");
                    sendNotification("Mock Location detected! Using last valid location.");
                }else{
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Location lastLocation = PreferenceLocationHelper.getLocation(getApplicationContext());
                    double lastLatitude = lastLocation.getLatitude();
                    double lastLongitude = lastLocation.getLongitude();

                    // Calculate the distance between the current location and the last stored location
                    float[] results = new float[1];
                    Location.distanceBetween(lastLatitude, lastLongitude, latitude, longitude, results);
                    float distanceInMeters = results[0];

                    // Check distance and decide whether to hit the API
                    if (distanceInMeters > 50) {
                        PreferenceLocationHelper.saveLocation(getApplicationContext(), latitude, longitude);
                        Log.d(TAG, "handleLocation: Distance more than 50 meters.");

                        PositionRequest request = new PositionRequest(String.valueOf(lastLatitude), String.valueOf(lastLongitude));
                        updatePositionApiCall(request);
                    } else {
                        // Do not hit the API
                        Log.d(TAG, "handleLocation: Distance less than 50 meters.");
                    }
                }
            } else {
                Log.d(TAG, "handleLocation: Location is null.");
            }
        } else {
            Log.d(TAG, "handleLocation: User is not logged in.");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking location in the background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);
        return builder.build();
    }

    private void initPreferenceChangeListener() {
        preferenceChangeListener = (sharedPreferences, key) -> {
            if (Constants.PREF_IS_LOGIN.equals(key)) {
                boolean isLogin = sharedPreferences.getBoolean(Constants.PREF_IS_LOGIN, false);
                if (!isLogin) {
                    Log.d(TAG, "User logged out. Stopping the service.");
                    stopSelf();
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void updatePositionApiCall(PositionRequest request) {
        String token = preferences.getString(Constants.PREF_TOKEN, null);
        positionRepository.updatePosition(token, request, new PositionRepository.UpdatePositionCallback() {
            @Override
            public void onSuccess(PositionResponse response) {
                Log.d("updatePositionApiCall", "message: " + response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                Log.e("updatePositionApiCall", "onError: " + t.getMessage());
            }
        });
    }

    private void sendNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mumla)
                .setContentTitle("LocationWorker Notification")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}
