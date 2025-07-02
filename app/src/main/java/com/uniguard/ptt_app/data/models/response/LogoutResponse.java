package com.uniguard.ptt_app.data.models.response;

import android.os.Parcel;
import android.os.Parcelable;

public class LogoutResponse implements Parcelable {
    public static final Parcelable.Creator<LogoutResponse> CREATOR = new Parcelable.Creator<LogoutResponse>() {
        public LogoutResponse createFromParcel(Parcel parcel) {
            return new LogoutResponse(parcel);
        }

        public LogoutResponse[] newArray(int i) {
            return new LogoutResponse[i];
        }
    };
    private String message;
    private boolean status;

    public int describeContents() {
        return 0;
    }

    public LogoutResponse(boolean z, String str) {
        this.status = z;
        this.message = str;
    }

    protected LogoutResponse(Parcel parcel) {
        this.status = parcel.readByte() != 0;
        this.message = parcel.readString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(this.status ? (byte) 1 : 0);
        parcel.writeString(this.message);
    }

    public boolean isStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
