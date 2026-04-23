package com.gcore.core;

import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.gcore.entity.am.ReceiverData;

public interface IBActivityThread extends IInterface {
    String DESCRIPTOR = "com.gcore.core.IBActivityThread";

    IBinder getActivityThread() throws RemoteException;
    void bindApplication() throws RemoteException;
    void restartJobService(String selfId) throws RemoteException;
    IBinder acquireContentProviderClient(ProviderInfo providerInfo) throws RemoteException;
    IBinder peekService(Intent intent) throws RemoteException;
    void stopService(Intent intent) throws RemoteException;
    void finishActivity(IBinder token) throws RemoteException;
    void handleNewIntent(IBinder token, Intent intent) throws RemoteException;
    void scheduleReceiver(ReceiverData data) throws RemoteException;

    abstract class Stub extends Binder implements IBActivityThread {
        static final int TRANSACTION_getActivityThread = 1;
        static final int TRANSACTION_bindApplication = 2;
        static final int TRANSACTION_restartJobService = 3;
        static final int TRANSACTION_acquireContentProviderClient = 4;
        static final int TRANSACTION_peekService = 5;
        static final int TRANSACTION_stopService = 6;
        static final int TRANSACTION_finishActivity = 7;
        static final int TRANSACTION_handleNewIntent = 8;
        static final int TRANSACTION_scheduleReceiver = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBActivityThread asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IBActivityThread) {
                return (IBActivityThread) iin;
            }
            return new Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code >= 1 && code <= 16777215) {
                data.enforceInterface(DESCRIPTOR);
            }
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            switch (code) {
                case TRANSACTION_getActivityThread:
                    IBinder result = getActivityThread();
                    reply.writeNoException();
                    reply.writeStrongBinder(result);
                    return true;
                case TRANSACTION_bindApplication:
                    bindApplication();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_restartJobService:
                    restartJobService(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_acquireContentProviderClient:
                    ProviderInfo providerInfo = _Parcel.readTypedObject(data, ProviderInfo.CREATOR);
                    IBinder binder = acquireContentProviderClient(providerInfo);
                    reply.writeNoException();
                    reply.writeStrongBinder(binder);
                    return true;
                case TRANSACTION_peekService:
                    Intent intent = _Parcel.readTypedObject(data, Intent.CREATOR);
                    IBinder serviceBinder = peekService(intent);
                    reply.writeNoException();
                    reply.writeStrongBinder(serviceBinder);
                    return true;
                case TRANSACTION_stopService:
                    stopService(_Parcel.readTypedObject(data, Intent.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finishActivity:
                    finishActivity(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handleNewIntent:
                    handleNewIntent(data.readStrongBinder(), _Parcel.readTypedObject(data, Intent.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_scheduleReceiver:
                    scheduleReceiver(_Parcel.readTypedObject(data, ReceiverData.CREATOR));
                    reply.writeNoException();
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IBActivityThread {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public IBinder getActivityThread() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getActivityThread, data, reply, 0);
                    reply.readException();
                    return reply.readStrongBinder();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void bindApplication() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_bindApplication, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void restartJobService(String selfId) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeString(selfId);
                    mRemote.transact(Stub.TRANSACTION_restartJobService, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public IBinder acquireContentProviderClient(ProviderInfo providerInfo) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    _Parcel.writeTypedObject(data, providerInfo, 0);
                    mRemote.transact(Stub.TRANSACTION_acquireContentProviderClient, data, reply, 0);
                    reply.readException();
                    return reply.readStrongBinder();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public IBinder peekService(Intent intent) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    _Parcel.writeTypedObject(data, intent, 0);
                    mRemote.transact(Stub.TRANSACTION_peekService, data, reply, 0);
                    reply.readException();
                    return reply.readStrongBinder();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void stopService(Intent intent) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    _Parcel.writeTypedObject(data, intent, 0);
                    mRemote.transact(Stub.TRANSACTION_stopService, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void finishActivity(IBinder token) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeStrongBinder(token);
                    mRemote.transact(Stub.TRANSACTION_finishActivity, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void handleNewIntent(IBinder token, Intent intent) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeStrongBinder(token);
                    _Parcel.writeTypedObject(data, intent, 0);
                    mRemote.transact(Stub.TRANSACTION_handleNewIntent, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            @Override
            public void scheduleReceiver(ReceiverData data) throws RemoteException {
                Parcel parcel = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    parcel.writeInterfaceToken(DESCRIPTOR);
                    _Parcel.writeTypedObject(parcel, data, 0);
                    mRemote.transact(Stub.TRANSACTION_scheduleReceiver, parcel, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    parcel.recycle();
                }
            }
        }
    }

    class _Parcel {
        static <T> T readTypedObject(Parcel parcel, Parcelable.Creator<T> creator) {
            if (parcel.readInt() != 0) {
                return creator.createFromParcel(parcel);
            }
            return null;
        }

        static <T extends Parcelable> void writeTypedObject(Parcel parcel, T t, int flags) {
            if (t == null) {
                parcel.writeInt(0);
                return;
            }
            parcel.writeInt(1);
            t.writeToParcel(parcel, flags);
        }
    }
}
