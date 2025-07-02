package com.uniguard.ptt_app.data.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        public User[] newArray(int i) {
            return new User[i];
        }
    };
    private String code;
    private String email;
    private int id;
    private String name;
    private Position position;

    public int describeContents() {
        return 0;
    }

    protected User(Parcel parcel) {
        this.id = parcel.readInt();
        this.name = parcel.readString();
        this.email = parcel.readString();
        this.code = parcel.readString();
        this.position = (Position) parcel.readParcelable(Position.class.getClassLoader());
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.email);
        parcel.writeString(this.code);
        parcel.writeParcelable(this.position, i);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getCode() {
        return this.code;
    }

    public Position getPosition() {
        return this.position;
    }
}
