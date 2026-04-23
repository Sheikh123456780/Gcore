package android.content;

import android.content.SyncResult;

interface ISyncContext {
    void sendHeartbeat();
    void onFinished(in SyncResult result);
}
