package com.uniguard.ptt_app.data.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class DefaultResponse<T extends Parcelable> implements Parcelable {
    private boolean status;
    private String message;

    @SerializedName("data")
    private T data;

    public DefaultResponse(boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    protected DefaultResponse(Parcel in) {
        status = in.readByte() != 0;
        message = in.readString();
        data = in.readParcelable(getClass().getClassLoader());
    }

    public static final Creator<DefaultResponse> CREATOR = new Creator<DefaultResponse>() {
        @Override
        public DefaultResponse createFromParcel(Parcel in) {
            return new DefaultResponse(in);
        }

        @Override
        public DefaultResponse[] newArray(int size) {
            return new DefaultResponse[size];
        }
    };

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeString(message);
        dest.writeParcelable(data, flags);
    }

    @Override
    public String toString() {
        return "DefaultResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
