package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public class ManifestDigest implements Parcelable {

    private ManifestDigest() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public String toString() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        throw new RuntimeException("Stub!");
    }

    public static final Creator<ManifestDigest> CREATOR = new Creator<>() {
        public ManifestDigest createFromParcel(Parcel source) {
            return new ManifestDigest();
        }

        public ManifestDigest[] newArray(int size) {
            return new ManifestDigest[size];
        }
    };

}