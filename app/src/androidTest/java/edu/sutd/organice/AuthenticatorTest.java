package edu.sutd.organice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.Resources;

import org.junit.Test;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AuthenticatorTest {
    private static final String TEST_ACC_NAME = "Organice-test";

    private AccountManager accountManager;
    private Resources resources;
    private Context context;
    private final String accType;

    public AuthenticatorTest() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        resources = context.getResources();
        accountManager = AccountManager.get(context);
        accType = resources.getString(R.string.account_type);
    }

    @Test
    public void testAccountExistsAfterCreation() {
        // ensure test account does not exist
        accountManager.removeAccountExplicitly(
                new Account(TEST_ACC_NAME, accType)
        );

        // add test account
        Authenticator.addAccount(context, TEST_ACC_NAME);

        Account[] accounts = accountManager.getAccountsByType(accType);
        for (Account acc: accounts) {
            if (acc.name.equals(TEST_ACC_NAME)) {
                boolean removeStatus = accountManager.removeAccountExplicitly(acc);
                assertEquals(removeStatus, true);
                return;
            }
        }
        fail("no account found");
    }

}
