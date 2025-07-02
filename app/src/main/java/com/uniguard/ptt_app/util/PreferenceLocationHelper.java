package com.uniguard.ptt_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class PreferenceLocationHelper {

    private static final String PREF_LOCATION_LAT = "PREF_LOCATION_LAT";
    private static final String PREF_LOCATION_LON = "PREF_LOCATION_LON";

    public static void saveLocation(Context context, double latitude, double longitude) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(PREF_LOCATION_LAT, Double.doubleToLongBits(latitude));
        editor.putLong(PREF_LOCATION_LON, Double.doubleToLongBits(longitude));
        editor.apply();
    }

    public static double getLatitude(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Double.longBitsToDouble(preferences.getLong(PREF_LOCATION_LAT, Double.doubleToLongBits(0.0)));
    }

    public static double getLongitude(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Double.longBitsToDouble(preferences.getLong(PREF_LOCATION_LON, Double.doubleToLongBits(0.0)));
    }

    public static Location getLocation(Context context) {
        double latitude = getLatitude(context);
        double longitude = getLongitude(context);
        Location location = new Location("storedLocation");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

}


