// IRemoteService.aidl
package com.cxy.test.aidlserver;
import com.cxy.test.aidlserver.ICallback;

// Declare any non-default types here with import statements

interface IRemoteService {
    void callRemoteService(ICallback callback);
}
