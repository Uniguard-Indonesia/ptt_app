package com.uniguard.ptt_app.data.models.request;

import android.os.Parcel;
import android.os.Parcelable;

public class PositionRequest implements Parcelable {
    public static final Parcelable.Creator<LoginRequest> CREATOR = new Parcelable.Creator<LoginRequest>() {
        public LoginRequest createFromParcel(Parcel parcel) {
            return new LoginRequest(parcel);
        }

        public LoginRequest[] newArray(int i) {
            return new LoginRequest[i];
        }
    };
    private String latitude;
    private String longitude;

    public int describeContents() {
        return 0;
    }

    public PositionRequest(String str, String str2) {
        this.latitude = str;
        this.longitude = str2;
    }

    protected PositionRequest(Parcel parcel) {
        this.latitude = parcel.readString();
        this.longitude = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.latitude);
        parcel.writeString(this.longitude);
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }
}
