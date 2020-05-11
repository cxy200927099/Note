package com.cxy.test.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class TestService extends Service {

    private static final String TAG = "TestService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "remote service onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new BindService();
    }

    static class BindService extends IRemoteService.Stub{

        @Override
        public void callRemoteService(ICallback callback) throws RemoteException {
            Log.i(TAG, "called remote service ");
            Log.i(TAG, "server do some work need 3s!");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "server do some work done!");
            callback.callback();
            Log.i(TAG, "server do callback done!");
        }
    }
}
