package android.content;

import android.content.Intent;
import android.os.Bundle;

interface IIntentReceiver {
    void performReceive(in Intent intent, int resultCode, String data, in Bundle extras, boolean ordered, boolean sticky, int sendingUser);
}

