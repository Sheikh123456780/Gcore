package android.content.pm;

interface IPackageDataObserver {
    void onRemoveCompleted(in String packageName, boolean succeeded);
}
