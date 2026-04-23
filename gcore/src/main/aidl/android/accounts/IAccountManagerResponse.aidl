package android.accounts;

import android.os.Bundle;

interface IAccountManagerResponse {
    void onResult(in Bundle value);
    void onError(int errorCode, String errorMessage);
}
