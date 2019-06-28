package com.snowdragon.whatsnext.database;

import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class around Firebase's FirebaseAuth.
 * <p>
 *     This class exposes methods in the FirebaseAuth related to
 *     the application only. Also it serves to provide methods and names
 *     to certain cryptic functions in the firebaseAuth API.
 * </p>
 * <p>
 *     Also this class allows one to get an intent to begin a sign in
 *     flow defined by FirebaseAuth UI. However, once the intent is passed over,
 *     it is up to the controller/UI thread to keep track of any events
 *     and to call methods again from this class if required to get the user logged in
 *     as this class has no idea about the application using it and has no access to the sign
 *     in flow that is defined by the Auth UI. As such, AuthStateListeners
 *     that are available by the FirebaseAuth is not available unless u specifically request
 *     for an instance of the FirebaseAuth in UI code which is not recommended.
 * </p>
 */
public class Auth {

    public static final int GOOGLE_PROVIDER = 0;
    public static final int EMAIL_PROVIDER = 1;
    public static final int PHONE_PROVIDER = 2;
    public static final int FACEBOOK_PROVIDER = 3;
    public static final int TWITTER_PROVIDER = 4;

    private final FirebaseAuth mFirebaseAuth;

    private Auth(FirebaseAuth auth) {
        mFirebaseAuth = auth;
    }

    /**
     * Gets an instance of Auth.
     *
     * @return An instance of Auth.
     */
    public static Auth getInstance() {
        return new Auth(FirebaseAuth.getInstance());
    }

    public boolean isCurrentUserSignedIn() {
        return mFirebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return mFirebaseAuth.getCurrentUser();
    }

    public void signOutCurrentUser() {
        mFirebaseAuth.signOut();
    }

    /**
     * Retrieves an intent for starting a sign in flow for user sign in.
     * <p>
     *     This method helps to get an intent to start a new activity
     *     that will start the sign in flow for a user. In dev builds,
     *     the smart lock feature that is available on FirebaseAuth is
     *     currently turned off as it can lead to sign in errors and
     *     persistent storage of accounts that make it hard to debug
     *     and test the app. However, for release builds, it is recommended
     *     to enable the smart lock feature for security purposes.
     * </p>
     * <p>
     *     Currently, you are able to provide the authentication providers
     *     of Google and Email only. Other auth providers will fail at sign in
     *     as they have not been enabled in the Firebase console.
     * </p>
     * @param authProviders The auth providers to enable on the sign in flow.
     * @return An intent to start the sign in flow activity.
     */
    public static Intent getAuthSignInIntent(int... authProviders) {
        List<AuthUI.IdpConfig> providers = getAuthProviders(authProviders);
        return AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(true)
                .build();
    }

    /**
     * Utility method to get a List of IdpConfig for Auth sign in.
     * <p>
     *     Helper method to determine which IdpConfig builders to make
     *     and enable.
     * </p>
     * @param authProviders The array of auth providers to enable for sign in.
     * @return A list of all the auth providers instances.
     */
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
