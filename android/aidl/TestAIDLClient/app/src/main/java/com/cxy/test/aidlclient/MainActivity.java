package com.cxy.test.aidlclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.cxy.test.aidlserver.ICallback;
import com.cxy.test.aidlserver.IRemoteService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "client";
    private static final String PKG_NAME = "com.cxy.test.aidlserver";
    private static final String CLASS_NAME = "com.cxy.test.aidlserver.TestService";


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final IRemoteService service1 =
                    IRemoteService.Stub.asInterface(service);
            try {
                Log.i(TAG,"connect to server! and call remote");
                service1.callRemoteService(mICallback);
                Log.i(TAG,"call remote done");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ICallback mICallback = new ICallback.Stub() {
        @Override
        public void callback() throws RemoteException {
            Log.i(TAG,"call callback!");
            Log.i(TAG, "client callback do work");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "client callback do work done!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "client main start!");

        Intent remoteService = new Intent();
        remoteService.setComponent(new ComponentName(PKG_NAME,CLASS_NAME));
        if(bindService(remoteService,mServiceConnection,
                Context.BIND_AUTO_CREATE)) {

        } else {
            unbindService(mServiceConnection);
        }
    }


}
