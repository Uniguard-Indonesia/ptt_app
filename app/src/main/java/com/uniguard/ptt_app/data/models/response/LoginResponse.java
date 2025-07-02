package com.uniguard.ptt_app.data.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.uniguard.ptt_app.data.models.ServerApi;

public class LoginResponse implements Parcelable {
    public static final Parcelable.Creator<LoginResponse> CREATOR = new Parcelable.Creator<LoginResponse>() {
        public LoginResponse createFromParcel(Parcel parcel) {
            return new LoginResponse(parcel);
        }

        public LoginResponse[] newArray(int i) {
            return new LoginResponse[i];
        }
    };
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("certificate_path")
    private String certificatePath;
    private String email;
    private String name;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("servers")
    private List<ServerApi> servers;

    public int describeContents() {
        return 0;
    }

    public LoginResponse(String str, String str2, String str3, String str4, String str5, List<ServerApi> list) {
        this.accessToken = str;
        this.refreshToken = str2;
        this.certificatePath = str3;
        this.email = str4;
        this.name = str5;
        this.servers = list;
    }

    protected LoginResponse(Parcel parcel) {
        this.accessToken = parcel.readString();
        this.refreshToken = parcel.readString();
        this.certificatePath = parcel.readString();
        this.email = parcel.readString();
        this.name = parcel.readString();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getCertificatePath() {
        return this.certificatePath;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public List<ServerApi> getServers() {
        return this.servers;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.accessToken);
        parcel.writeString(this.refreshToken);
        parcel.writeString(this.certificatePath);
        parcel.writeString(this.email);
        parcel.writeString(this.name);
    }
}
