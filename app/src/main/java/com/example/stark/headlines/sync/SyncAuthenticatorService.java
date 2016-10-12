package com.example.stark.headlines.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by stark on 8/10/16.
 */

public class SyncAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private SyncAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new SyncAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
