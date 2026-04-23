package com.gcore.proxy;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.gcore.app.GActivityThread;
import com.gcore.fake.hook.HookManager;
import com.gcore.fake.service.HCallbackProxy;
import com.gcore.proxy.record.ProxyActivityRecord;

public class ProxyActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        HookManager.get().checkEnv(HCallbackProxy.class);

        ProxyActivityRecord record = ProxyActivityRecord.create(getIntent());
        if (record.mTarget != null) {
            record.mTarget.setExtrasClassLoader(GActivityThread.getApplication().getClassLoader());
            startActivity(record.mTarget);
        }
    }

    public static class P0 extends ProxyActivity {

    }

    public static class P1 extends ProxyActivity {

    }

    public static class P2 extends ProxyActivity {

    }

    public static class P3 extends ProxyActivity {

    }

    public static class P4 extends ProxyActivity {

    }

    public static class P5 extends ProxyActivity {

    }

    public static class P6 extends ProxyActivity {

    }

    public static class P7 extends ProxyActivity {

    }

    public static class P8 extends ProxyActivity {

    }

    public static class P9 extends ProxyActivity {

    }

    public static class P10 extends ProxyActivity {

    }

    public static class P11 extends ProxyActivity {

    }

    public static class P12 extends ProxyActivity {

    }

    public static class P13 extends ProxyActivity {

    }

    public static class P14 extends ProxyActivity {

    }

    public static class P15 extends ProxyActivity {

    }

    public static class P16 extends ProxyActivity {

    }

    public static class P17 extends ProxyActivity {

    }

    public static class P18 extends ProxyActivity {

    }

    public static class P19 extends ProxyActivity {

    }

    public static class P20 extends ProxyActivity {

    }

    public static class P21 extends ProxyActivity {

    }

    public static class P22 extends ProxyActivity {

    }

    public static class P23 extends ProxyActivity {

    }

    public static class P24 extends ProxyActivity {

    }

}
