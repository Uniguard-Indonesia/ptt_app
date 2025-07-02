package com.uniguard.ptt_app.data.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Position implements Parcelable {
    public static final Parcelable.Creator<Position> CREATOR = new Parcelable.Creator<Position>() {
        public Position createFromParcel(Parcel parcel) {
            return new Position(parcel);
        }

        public Position[] newArray(int i) {
            return new Position[i];
        }
    };
    @SerializedName("created_at")
    private String createdAt;
    private String latitude;
    private String longitude;

    public int describeContents() {
        return 0;
    }

    public Position(String str, String str2, String str3) {
        this.latitude = str;
        this.longitude = str2;
        this.createdAt = str3;
    }

    protected Position(Parcel parcel) {
        this.latitude = parcel.readString();
        this.longitude = parcel.readString();
        this.createdAt = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.latitude);
        parcel.writeString(this.longitude);
        parcel.writeString(this.createdAt);
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }
}
