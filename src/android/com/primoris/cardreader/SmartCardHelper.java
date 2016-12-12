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
import android.content.Context;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import android.util.Log;
import android.content.BroadcastReceiver;
import com.telpo.tps550.api.magnetic.MagneticCard;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.TimeoutException;
import com.telpo.tps550.api.reader.ReaderMonitor;
import com.telpo.tps550.api.reader.SmartCardReader;
import java.io.UnsupportedEncodingException;
import android.content.IntentFilter;

public class SmartCardHelper extends CordovaPlugin {

    @Override
    protected void pluginInitialize() {
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        // TODO Auto-generated method stub
        super.initialize(cordova, webView);
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
        if (action.equals("startMonitor")) {
            System.out.println("\n\n\n In Action == startMonitor \n\n\n\n");
            try {
                this.startMonitor();
                callbackContext.success("Successssssss startMonitor");
            } catch (Exception ex) {
                System.out.println("in teklpo exception");
            }
        } else {
            retValue = false;
        }

        return retValue;
    }

    public boolean startMonitor() throws TelpoException {
        ReaderMonitor.setContext(this.activity);
        ReaderMonitor.startMonitor();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ReaderMonitor.ACTION_ICC_PRESENT);
        callbackContext.getContext().registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("123", "icc present Broadcast Receiver");
            if (intent.getAction() == ReaderMonitor.ACTION_ICC_PRESENT) {
                if (intent.getExtras().getBoolean(ReaderMonitor.EXTRA_IS_PRESENT)) {
                    int cardType = intent.getExtras().getInt(ReaderMonitor.EXTRA_CARD_TYPE);
                    if (cardType == CardReader.CARD_TYPE_SLE4428) {
                        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<SLE4428>>>>>>>>>>>>>>>>>>>>>");
                    } else if (cardType == CardReader.CARD_TYPE_SLE4442) {
                        System.out.println("<<<<<<<<<<<<<<<SLE4442>>>>>>>>>>>>>>>>>>>");
                    } else if (cardType == CardReader.CARD_TYPE_ISO7816) {
                        System.out.println("<<<<<<<<<<<<<<SMART CARD>>>>>>>>>>>>>>>>>>>");
                    } else {
                        System.out.println("<<<<<<<<<<<Unknown>>>>>>>>>>>>>");
                    }
                } else {
                    System.out.println("<<<<<<<<<<<<<<<NO Card>>>>>>>>>>>>>>>>>>>");
                }
            }
        }

    };


}
