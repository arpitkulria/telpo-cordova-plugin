//  Copyright (c) 2016 PayPal. All rights reserved.

package com.primoris.cardreader;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import android.util.Log;
import com.telpo.tps550.api.magnetic.MagneticCard;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.TimeoutException;
import java.io.UnsupportedEncodingException;


public class MagneticCardHelper extends CordovaPlugin {

    @Override
    protected void pluginInitialize() {
//        System.loadLibrary("src/android/lib/native");
//        System.loadLibrary("src/android/lib/armeabi");
        System.out.println("\n\n\n In pluginInitialize function \n\n\n\n");
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        // TODO Auto-generated method stub
        super.initialize(cordova, webView);
        System.out.println("\n\n\n In initialize function \n\n\n\n");
        Log.d("---------------------SAMPLE_ECHO---------------------", "--------------------- initializing ---------------------");
    }

    private CallbackContext callbackContext;
    private Activity activity = null;
    private static final int REQUEST_CARD_SCAN = 10;

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.activity = this.cordova.getActivity();
        boolean retValue = true;
        if (action.equals("open")) {
            System.out.println("\n\n\n In Action == open \n\n\n\n");
            try {
                this.open();
            } catch (Exception ex) {
                System.out.println("in teklpo exception");
            }
        } /*else if (action.equals("")) {
            //
        } else if (action.equals("")) {
            //
        }*/ else {
            retValue = false;
        }

        return retValue;
    }

    public static void open() throws TelpoException {
        System.out.println("\n\n\n\n --------------------IN OPEN FUNCTION JAVA------------------------ \n\n\n\n\n ");
        MagneticCard.open();
    }
}
