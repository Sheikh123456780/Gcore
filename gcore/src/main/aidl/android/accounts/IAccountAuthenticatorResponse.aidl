package android.accounts;
import android.os.Bundle;

interface IAccountAuthenticatorResponse {
    void onResult(in Bundle value);
    void onRequestContinued();
    void onError(int errorCode, String errorMessage);
}
