package com.msoft.mspartners;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstansIDService extends FirebaseInstanceIdService {

    private static final String TAG ="MyFirebase";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "token: " + token);
        sendRegistrationToServer(token);
    }

    private  void sendRegistrationToServer(String token){ }
}