package edu.sutd.organice;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {

    private static final String LOG_TAG = "Authenticator";

    public Authenticator(Context context) {
        super(context);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse r,
            String s,
            String s2,
            String[] strings,
            Bundle bundle
    ) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle
    ) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle
    ) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle
    ) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account,
            String[] strings
    ) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    public static void addAccount(Context context, String name) {
        Resources resources = context.getResources();
        final String ACCOUNT_TYPE = resources.getString(R.string.account_type);

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // loop through all Organice accounts, return if account with given name is found
        Account[] accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type));
        for(Account acc: accounts) {
            if (acc.name.equals(name)) {
                Log.d(LOG_TAG, "account \"" + name + "\" already exists");
                return;
            }
        }

        // account not found, create it
        Log.i(LOG_TAG, "creating account \"" + name + "\"");
        Account newAccount = new Account(name, ACCOUNT_TYPE);
        if(!accountManager.addAccountExplicitly(newAccount, null, null)) {
            Log.e(LOG_TAG, "failed to add account");
            Log.e(LOG_TAG, "there are " + Integer.toString(accounts.length) + " Organice accounts");
        }
    }
}
