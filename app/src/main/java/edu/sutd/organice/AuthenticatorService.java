package edu.sutd.organice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This class is a stub authenticator service used to allow the app to create an account to access
 * Android's calendar provider.
 */
public class AuthenticatorService extends Service {

    private Authenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
