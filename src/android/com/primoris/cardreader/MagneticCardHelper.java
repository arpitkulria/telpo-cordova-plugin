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
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
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
        if (action.equals("open")) {
            System.out.println("\n\n\n In Action == open \n\n\n\n");
            try {
                this.open();
                callbackContext.success("Successssssss opeeeeenn");
            } catch (Exception ex) {
                System.out.println("in teklpo exception");
            }
        } else if (action.equals("startReading")) {
            System.out.println("\n\n\n In Action == startReading \n\n\n\n");
            try {
                String[] ans = this.startReading();
                callbackContext.success(Arrays.toString(ans));
            } catch (Exception ex) {
                System.out.println("in teklpo exception");
            }
        } else if(action.equals("startMonitor")) {
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

    public static void open() throws TelpoException {
        MagneticCard.open();
    }

    public static String[] startReading() throws TelpoException {
        MagneticCard.startReading();
        String[] arr = MagneticCard.check(10000);
        return arr;
    }

    public boolean startMonitor() throws TelpoException {
        ReaderMonitor.setContext(this.activity);
        ReaderMonitor.startMonitor();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ReaderMonitor.ACTION_ICC_PRESENT);

        this.activity.registerReceiver(mReceiver, filter);
        return true;
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
