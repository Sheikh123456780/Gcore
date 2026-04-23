package android.accounts;

import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.Account;
import android.os.Bundle;

interface IAccountAuthenticator {
    void addAccount(in IAccountAuthenticatorResponse response, String accountType, String authTokenType, in String[] requiredFeatures, in Bundle options);
    void confirmCredentials(in IAccountAuthenticatorResponse response, in Account account, in Bundle options);
    void getAuthToken(in IAccountAuthenticatorResponse response, in Account account, String authTokenType, in Bundle options);
    void getAuthTokenLabel(in IAccountAuthenticatorResponse response, String authTokenType);
    void updateCredentials(in IAccountAuthenticatorResponse response, in Account account, String authTokenType, in Bundle options);
    void editProperties(in IAccountAuthenticatorResponse response, String accountType);
    void hasFeatures(in IAccountAuthenticatorResponse response, in Account account, in String[] features);
    void getAccountRemovalAllowed(in IAccountAuthenticatorResponse response, in Account account);
    void getAccountCredentialsForCloning(in IAccountAuthenticatorResponse response,  in Account account);
    void addAccountFromCredentials(in IAccountAuthenticatorResponse response, in Account account, in Bundle accountCredentials);
}
