package android.content.pm;

import android.content.IntentFilter;
import android.os.Bundle;

import java.util.ArrayList;

public class PackageParser {

    public PackageParser() {
        throw new RuntimeException("Stub!");
    }

    public PackageParser(final String archiveSourcePath) {
        throw new RuntimeException("Stub!");
    }

    public Package parsePackage() throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    public Package parsePackage(final int flags) {
        throw new RuntimeException("Stub!");
    }

    public void collectCertificates() throws PackageParserException {
        throw new RuntimeException("Stub!");
    }

    public final static class Package {

        public String packageName;

        public String baseCodePath;

        public ApplicationInfo applicationInfo = new ApplicationInfo();

        public final ArrayList<Permission> permissions = new ArrayList<>(0);
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);
        public final ArrayList<Activity> activities = new ArrayList<>(0);
        public final ArrayList<Activity> receivers = new ArrayList<>(0);
        public final ArrayList<Provider> providers = new ArrayList<>(0);
        public final ArrayList<Service> services = new ArrayList<>(0);
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<>(0);

        public final ArrayList<String> requestedPermissions = new ArrayList<>();

        public ArrayList<String> usesLibraries;
        public ArrayList<String> usesOptionalLibraries;

        public Bundle mAppMetaData;

        public int mVersionCode;

        public String mVersionName;

        public String mSharedUserId;

        public int mSharedUserLabel;

        public Signature[] mSignatures;
        public SigningDetails mSigningDetails;

        public int mPreferredOrder;

        public ArrayList<ConfigurationInfo> configPreferences;

        public ArrayList<FeatureInfo> reqFeatures;

        public Package() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public static class Component<II extends IntentInfo> {
        public final ArrayList<II> intents;
        public final String className;
        public Bundle metaData;

        public Component() {
            throw new RuntimeException("Stub!");
        }

        public Component(final PackageItemInfo outInfo) {
            throw new RuntimeException("Stub!");
        }

        public Component(final ComponentInfo outInfo) {
            throw new RuntimeException("Stub!");
        }

    }

    public final static class Permission extends Component<IntentInfo> {
        public final PermissionInfo info;

        public Permission() {
            super();
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public final static class PermissionGroup extends Component<IntentInfo> {
        public final PermissionGroupInfo info;

        public PermissionGroup() {
            super();
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public final static class Activity extends Component<ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(final ActivityInfo info) {
            super(info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public final static class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(final ServiceInfo info) {
            super(info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public final static class Provider extends Component<ProviderIntentInfo> {
        public final ProviderInfo info;

        public Provider(final ProviderInfo info) {
            super(info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public final static class Instrumentation extends Component<IntentInfo> {
        public final InstrumentationInfo info;

        public Instrumentation(final InstrumentationInfo info) {
            super(info);
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public static class IntentInfo extends IntentFilter {
        public boolean hasDefault;
        public int labelRes;
        public CharSequence nonLocalizedLabel;
        public int icon;
        public int logo;
        public int banner;
    }

    public final static class ActivityIntentInfo extends IntentInfo {
        public final Activity activity;

        public ActivityIntentInfo() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public final static class ServiceIntentInfo extends IntentInfo {
        public final Service service;

        public ServiceIntentInfo() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        public final Provider provider;

        public ProviderIntentInfo() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }

    public static class PackageParserException extends Exception {

        public PackageParserException(String detailMessage) {
            super(detailMessage);
            throw new RuntimeException("Stub!");
        }

    }

    public static class SigningDetails {
        public Signature[] signatures;
        public Signature[] pastSigningCertificates;
    }
}