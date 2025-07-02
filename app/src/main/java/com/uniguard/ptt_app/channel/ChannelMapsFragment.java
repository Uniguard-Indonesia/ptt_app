package com.uniguard.ptt_app.channel;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.uniguard.ptt_app.Constants;
import com.uniguard.ptt_app.R;
import com.uniguard.ptt_app.data.models.User;
import com.uniguard.ptt_app.data.models.request.PositionRequest;
import com.uniguard.ptt_app.data.models.response.DefaultListResponse;
import com.uniguard.ptt_app.data.models.response.PositionResponse;
import com.uniguard.ptt_app.repository.PositionRepository;
import com.uniguard.ptt_app.util.HumlaServiceFragment;
import com.uniguard.ptt_app.util.LocationUtils;
import com.uniguard.ptt_app.util.PreferenceLocationHelper;

public class ChannelMapsFragment extends HumlaServiceFragment implements OnMapReadyCallback {
    private int currentMapType = 1;
    private GoogleMap gMap;
    private PositionRepository positionRepository;
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        positionRepository = new PositionRepository();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_map_type) {
            toggleMapType();
            updateMapTypeIcon(item);
            return true;
        } else if (item.getItemId() == R.id.menu_refresh_map) {
            refreshLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        googleMap.setMapType(this.currentMapType);
        getPositions();
    }

    private void refreshLocation() {
        if (getContext() != null) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

            // Check permissions (you may need to handle permission requests separately)
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Handle the case where permissions are not granted
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    if (LocationUtils.isMockLocation(location)) {
                        Log.d("ChannelMapsFragment", "Unable to get location.");
                        Toast.makeText(getContext(), "Cannot update location with mocked location or fake GPS.", Toast.LENGTH_LONG).show();
                    } else {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Save the location to SharedPreferences
                        PreferenceLocationHelper.saveLocation(getContext(), latitude, longitude);
                        updateMap(latitude, longitude);

                        Location lastLocation = PreferenceLocationHelper.getLocation(getContext());
                        PositionRequest request = new PositionRequest(String.valueOf(lastLocation.getLatitude()), String.valueOf(lastLocation.getLongitude()));
                        updatePositionApiCall(request);
                    }
                } else {
                    // Handle case where location is null (e.g., show an error message)
                    Log.d("ChannelMapsFragment", "Unable to get location.");
                }
            });
        }
    }


    private void toggleMapType() {
        if (this.currentMapType == 1) {
            this.currentMapType = GoogleMap.MAP_TYPE_HYBRID;
        } else {
            this.currentMapType = GoogleMap.MAP_TYPE_NORMAL;
        }
        this.gMap.setMapType(this.currentMapType);
        this.preferences.edit().putInt(Constants.MAP_TYPE_PREF, this.currentMapType).apply();
    }

    private void updateMapTypeIcon(MenuItem menuItem) {
        if (currentMapType == GoogleMap.MAP_TYPE_HYBRID) {
            menuItem.setIcon(R.drawable.map_active);
        } else if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) {
            menuItem.setIcon(R.drawable.map_inactive);
        }
    }

    private void updateMap(double latitude, double longitude) {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                LatLng location = new LatLng(latitude, longitude);
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(location).title("Uniguard ID"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
            });
        }
    }

    private void updatePositionApiCall(PositionRequest request) {
        String token = preferences.getString(Constants.PREF_TOKEN, null);
        positionRepository.updatePosition(token, request, new PositionRepository.UpdatePositionCallback() {
            @Override
            public void onSuccess(PositionResponse response) {
                getPositions();
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable t) {
                Log.e("updatePositionApiCall", "onError: " + t.getMessage());
            }
        });
    }

    private void getPositions() {
        String token = preferences.getString(Constants.PREF_TOKEN, null);
        positionRepository.getPositions(token, new PositionRepository.GetPositionsCallback() {
            @Override
            public void onSuccess(DefaultListResponse<User> response) {
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(googleMap -> {
                        googleMap.clear();
                        for (User user : response.getData()) {
                            String latitudeStr = user.getPosition().getLatitude();
                            String longitudeStr = user.getPosition().getLongitude();

                            double latitude = Double.parseDouble(latitudeStr);
                            double longitude = Double.parseDouble(longitudeStr);

                            LatLng location = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions().position(location)
                                    .title(user.getEmail())
                                    .snippet(convertDateFormat(user.getPosition().getCreatedAt(), user.getCode()))
                            );
                        }
                        Location lastLocation = PreferenceLocationHelper.getLocation(getContext());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 14));
                    });
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.e("GetPositions", "onError: " + t.getMessage());

            }
        });
    }

    public String convertDateFormat(String time, String code) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.US);
        simpleDateFormat2.setTimeZone(TimeZone.getTimeZone(code));
        try {
            return simpleDateFormat2.format(simpleDateFormat.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

}
