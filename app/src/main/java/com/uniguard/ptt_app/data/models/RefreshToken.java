package com.uniguard.ptt_app.data.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class RefreshToken implements Parcelable {
    public static final Parcelable.Creator<RefreshToken> CREATOR = new Parcelable.Creator<RefreshToken>() {
        public RefreshToken createFromParcel(Parcel parcel) {
            return new RefreshToken(parcel);
        }

        public RefreshToken[] newArray(int i) {
            return new RefreshToken[i];
        }
    };
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;

    public int describeContents() {
        return 0;
    }

    public RefreshToken(String str, String str2) {
        this.accessToken = str;
        this.tokenType = str2;
    }

    protected RefreshToken(Parcel parcel) {
        this.accessToken = parcel.readString();
        this.tokenType = parcel.readString();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getTokenType() {
        return this.tokenType;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.accessToken);
        parcel.writeString(this.tokenType);
    }
}
