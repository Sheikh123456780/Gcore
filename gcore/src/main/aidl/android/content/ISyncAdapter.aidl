package android.content;

import android.accounts.Account;
import android.os.Bundle;
import android.content.ISyncContext;

interface ISyncAdapter {
    void startSync(ISyncContext syncContext, String authority, in Account account, in Bundle extras);
    void cancelSync(ISyncContext syncContext);
    void initialize(in Account account, String authority);
}
