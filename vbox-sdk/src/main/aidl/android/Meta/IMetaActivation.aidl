// IMetaActivation.aidl
package android.Meta;

interface IMetaActivation {
    void activateSdk(String userkey);
    boolean getActivatedSdk();
    String getServerMessage();
    boolean getNetwork();
}