package android.app;

interface IStopUserCallback {
    void userStopped(int userId);
    void userStopAborted(int userId);
}
