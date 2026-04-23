package com.gcore.core.system.user;

import android.os.Process;
import android.os.Parcel;
import android.os.Parcelable;

public final class BUserHandle implements Parcelable {

    public static final int PER_USER_RANGE = 100000;

    public static final int USER_ALL = -1;

    public static final int USER_XPOSED = -4;

    public static final int USER_NULL = -10000;

    public static final int USER_OWNER = 0;

    public static final BUserHandle OWNER = new BUserHandle(USER_OWNER);

    public static final int USER_SYSTEM = 0;

    public static final BUserHandle SYSTEM = new BUserHandle(USER_SYSTEM);

    public static final boolean MU_ENABLED = true;

    public static final int AID_APP_START = Process.FIRST_APPLICATION_UID;

    final int mHandle;

    public static int getUserId(int uid) {
        if (MU_ENABLED) {
            return uid / PER_USER_RANGE;
        } else {
            return BUserHandle.USER_SYSTEM;
        }
    }

    public static int getUid(int userId, int appId) {
        if (MU_ENABLED) {
            return userId * PER_USER_RANGE + (appId % PER_USER_RANGE);
        } else {
            return appId;
        }
    }

    public static int getAppId(int uid) {
        return uid % PER_USER_RANGE;
    }

    @Deprecated
    public boolean isOwner() {
        return this.equals(OWNER);
    }

    public boolean isSystem() {
        return this.equals(SYSTEM);
    }

    public BUserHandle(int h) {
        mHandle = h;
    }

    @Override
    public int hashCode() {
        return mHandle;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mHandle);
    }

    public static void writeToParcel(BUserHandle h, Parcel out) {
        if (h != null) {
            h.writeToParcel(out, 0);
        } else {
            out.writeInt(USER_NULL);
        }
    }

    public static final Parcelable.Creator<BUserHandle> CREATOR = new Creator<>() {
        public BUserHandle createFromParcel(Parcel in) {
            return new BUserHandle(in);
        }

        public BUserHandle[] newArray(int size) {
            return new BUserHandle[size];
        }
    };

    public BUserHandle(Parcel in) {
        mHandle = in.readInt();
    }
}
