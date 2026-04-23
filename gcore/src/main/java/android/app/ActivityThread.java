package android.app;

import android.os.Handler;

public class ActivityThread {

    public static ActivityThread currentActivityThread() {
        throw new RuntimeException();
    }

    public String getProcessName() {
        throw new RuntimeException();
    }

    public Handler getHandler() {
        throw new RuntimeException();
    }

}
