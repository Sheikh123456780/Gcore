package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public class VerifierInfo implements Parcelable {

    public static final Creator<VerifierInfo> CREATOR = new Creator<>() {
        public VerifierInfo createFromParcel(final Parcel source) {
            return new VerifierInfo();
        }

        public VerifierInfo[] newArray(final int size) {
            return new VerifierInfo[size];
        }
    };

    private VerifierInfo() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        throw new RuntimeException("Stub!");
    }
}