package com.snowdragon.whatsnext.database;

import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Auth {

    public static final int GOOGLE_PROVIDER = 0;
    public static final int EMAIL_PROVIDER = 1;
    public static final int PHONE_PROVIDER = 2;
    public static final int FACEBOOK_PROVIDER = 3;
    public static final int TWITTER_PROVIDER = 4;

    private static FirebaseAuth sFirebaseAuth;

    private Auth() {

    }

    public static Auth getInstance() {
        if(sFirebaseAuth == null) {
            sFirebaseAuth = FirebaseAuth.getInstance();
        }
        return new Auth();
    }

    public boolean isCurrentUserSignedIn() {
        return sFirebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return sFirebaseAuth.getCurrentUser();
    }

    public static Intent getAuthSignInIntent(int... authProviders) {
        List<AuthUI.IdpConfig> providers = getAuthProviders(authProviders);
        Intent intent = AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
        return intent;
    }

    private static List<AuthUI.IdpConfig> getAuthProviders(int[] authProviders) {
        List<AuthUI.IdpConfig> providers = new ArrayList<>();
        for(int provider : authProviders) {
            switch(provider) {
                case GOOGLE_PROVIDER :
                    providers.add(new AuthUI.IdpConfig.GoogleBuilder().build());
                    break;
                case EMAIL_PROVIDER :
                    providers.add(new AuthUI.IdpConfig.EmailBuilder().build());
                    break;
                case PHONE_PROVIDER :
                    providers.add(new AuthUI.IdpConfig.PhoneBuilder().build());
                    break;
                case FACEBOOK_PROVIDER :
                    providers.add(new AuthUI.IdpConfig.FacebookBuilder().build());
                    break;
                case TWITTER_PROVIDER :
                    providers.add(new AuthUI.IdpConfig.TwitterBuilder().build());
                    break;
            }
        }
        return providers;
    }
}
