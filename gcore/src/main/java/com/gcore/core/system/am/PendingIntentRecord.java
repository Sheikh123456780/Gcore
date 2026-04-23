package com.gcore.core.system.am;

import java.util.Objects;

public class PendingIntentRecord {

    public int uid;
    public String packageName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PendingIntentRecord that)) {
            return false;
        }
        return uid == that.uid && Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, packageName);
    }
}
