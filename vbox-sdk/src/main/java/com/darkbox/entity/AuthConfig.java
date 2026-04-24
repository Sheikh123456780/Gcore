package com.darkbox.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AuthConfig implements Parcelable {
    public String consumerKey;
    public String consumerSecret;

    protected AuthConfig(Parcel in) {
        consumerKey = in.readString();
        consumerSecret = in.readString();
    }

    public static final Creator<AuthConfig> CREATOR = new Creator<AuthConfig>() {
        public AuthConfig createFromParcel(Parcel in) {
            return new AuthConfig(in);
        }

        public AuthConfig[] newArray(int size) {
            return new AuthConfig[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(consumerKey);
        parcel.writeString(consumerSecret);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

