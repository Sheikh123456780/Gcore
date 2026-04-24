package com.darkbox.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stub class to prevent crash from BadParcelableException during Facebook login inside virtual environment like BlackBox.
 */
public class AccessToken implements Parcelable {
    public String token;
    public String userId;

    public AccessToken(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    protected AccessToken(Parcel in) {
        token = in.readString();
        userId = in.readString();
    }

    public static final Creator<AccessToken> CREATOR = new Creator<AccessToken>() {
        @Override
        public AccessToken createFromParcel(Parcel in) {
            return new AccessToken(in);
        }

        @Override
        public AccessToken[] newArray(int size) {
            return new AccessToken[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
        dest.writeString(userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}