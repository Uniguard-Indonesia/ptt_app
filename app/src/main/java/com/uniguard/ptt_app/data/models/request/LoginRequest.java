package com.uniguard.ptt_app.data.models.request;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginRequest implements Parcelable {
    public static final Parcelable.Creator<LoginRequest> CREATOR = new Parcelable.Creator<LoginRequest>() {
        public LoginRequest createFromParcel(Parcel parcel) {
            return new LoginRequest(parcel);
        }

        public LoginRequest[] newArray(int i) {
            return new LoginRequest[i];
        }
    };
    private String email;
    private String password;

    public int describeContents() {
        return 0;
    }

    public LoginRequest(String str, String str2) {
        this.email = str;
        this.password = str2;
    }

    protected LoginRequest(Parcel parcel) {
        this.email = parcel.readString();
        this.password = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.email);
        parcel.writeString(this.password);
    }

    public String toString() {
        return "LoginRequest{email='" + this.email + "', password='" + this.password + "'}";
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
}
