//  Copyright (c) 2016 PayPal. All rights reserved.

package io.card.cordova.sdk;

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
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import android.util.Log;

class TelpoException extends Exception {
    private static final long serialVersionUID = 1136193940236894072L;

    public TelpoException() {
    }

    public TelpoException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TelpoException(String detailMessage) {
        super(detailMessage);
    }

    public TelpoException(Throwable throwable) {
        super(throwable);
    }

    public String getDescription() {
        return "Exception occur during telpo device operation!";
    }
}


public class MagneticCard extends CordovaPlugin {

    @Override
    protected void pluginInitialize() {
//        System.loadLibrary("src/android/lib/native");
//        System.loadLibrary("src/android/lib/armeabi");
        System.loadLibrary("telpo_msr");
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)     {
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
            } catch (Exception ex){
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

    static {
        System.loadLibrary("telpo_msr");
    }

    public MagneticCard() {
    }

    public static synchronized void open() throws TelpoException {
        System.out.println("\n\n\n\n --------------------IN OPEN FUNCTION JAVA------------------------ \n\n\n\n\n ");
        int ret = open_msr();
        switch(ret) {
        case -3:
            throw new TelpoException("Cannot open magnetic stripe card reader!");
        case -2:
//            return PluginResult.Status.OK;
            throw new TelpoException("The magnetic stripe card reader has been opened!");
        default:
            throw new TelpoException("The magnetic stripe card reader default case");
        }
    }

    public static synchronized void close() {
        System.out.println("\n\n\n\n --------------------IN CLOSE FUNCTION JAVA------------------------ \n\n\n\n\n ");
        close_msr();
    }

    public static synchronized String[] check(int timeout) throws TelpoException {
        System.out.println("\n\n\n\n --------------------IN CHECK FUNCTION JAVA------------------------ \n\n\n\n\n ");
        byte[] result = new byte[256];
        int ret = check_msr(timeout, result);
        switch(ret) {
        case -4:
            throw new TelpoException("Timeout to get magnetic stripe card data!");
        case -3:
            throw new TelpoException("Cannot get magnetic stripe card data!");
        case -2:
        default:
            return ParseData(ret, result);
        case -1:
            throw new TelpoException("The magnetic stripe card reader has not been opened!");
        }
    }

    private static String[] ParseData(int size, byte[] data) throws TelpoException {
        String[] result = new String[3];
        byte pos = 0;
        byte len = data[pos];
        if(len == 0) {
            result[0] = "";
        } else {
            try {
                result[0] = new String(data, pos + 1, len, "GBK");

            }catch (Exception ex){
                System.out.println("in teklpo exception");
            }
        }

        int pos1 = data[0] + 1;
        len = data[pos1];
        if(len == 0) {
            result[1] = "";
        } else {
            try {
                result[1] = new String(data, pos1 + 1, len, "GBK");
            } catch (Exception var7) {
                var7.printStackTrace();
                throw new TelpoException();
            }
        }

        pos1 += data[pos1] + 1;
        len = data[pos1];
        if(len == 0) {
            result[2] = "";
        } else {
            try {
                result[2] = new String(data, pos1 + 1, len, "GBK");
            } catch (Exception var6) {
                var6.printStackTrace();
                throw new TelpoException();
            }
        }

        return result;
    }

    public static int startReading() {
        return ready_for_read();
    }

    private static native int open_msr();

    private static native void close_msr();

    private static native int check_msr(int var0, byte[] var1);

    private static native int ready_for_read();
}

/*


public class CardIOCordovaPlugin1 extends CordovaPlugin {

    private CallbackContext callbackContext;
    private Activity activity = null;
    private static final int REQUEST_CARD_SCAN = 10;

    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.activity = this.cordova.getActivity();
        boolean retValue = true;
        if (action.equals("scan")) {
            this.scan(args);
        } else if (action.equals("canScan")) {
            this.canScan(args);
        } else if (action.equals("version")) {
            this.callbackContext.success(CardIOActivity.sdkVersion());
        } else {
            retValue = false;
        }

        return retValue;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void prepareToRender(JSONArray args) throws JSONException {
        this.callbackContext.success();
    }

    private void scan(JSONArray args) throws JSONException {
        Intent scanIntent = new Intent(this.activity, CardIOActivity.class);
        JSONObject configurations = args.getJSONObject(0);
        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, this.getConfiguration(configurations, "requireExpiry", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, this.getConfiguration(configurations, "requireCVV", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, this.getConfiguration(configurations, "requirePostalCode", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, this.getConfiguration(configurations, "suppressManual", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, this.getConfiguration(configurations, "restrictPostalCodeToNumericOnly", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, this.getConfiguration(configurations, "keepApplicationTheme", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, this.getConfiguration(configurations, "requireCardholderName", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, this.getConfiguration(configurations, "useCardIOLogo", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_INSTRUCTIONS, this.getConfiguration(configurations, "scanInstructions", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_NO_CAMERA, this.getConfiguration(configurations, "noCamera", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, this.getConfiguration(configurations, "scanExpiry", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE, this.getConfiguration(configurations, "languageOrLocale", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, this.getConfiguration(configurations, "guideColor", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, this.getConfiguration(configurations, "suppressConfirmation", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, this.getConfiguration(configurations, "hideCardIOLogo", false)); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_SCAN, this.getConfiguration(configurations, "suppressScan", false)); // default: false
        this.cordova.startActivityForResult(this, scanIntent, REQUEST_CARD_SCAN);
    }

    private void canScan(JSONArray args) throws JSONException {
        if (CardIOActivity.canReadCardWithCamera()) {
            // This is where we return if scanning is enabled.
            this.callbackContext.success("Card Scanning is enabled");
        } else {
            this.callbackContext.error("Card Scanning is not enabled");
        }
    }

    // onActivityResult
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (REQUEST_CARD_SCAN == requestCode) {
            if (resultCode == CardIOActivity.RESULT_CARD_INFO) {
                CreditCard scanResult = null;
                if (intent.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                    scanResult = intent
                            .getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                    this.callbackContext.success(this.toJSONObject(scanResult));
                } else {
                    this.callbackContext
                            .error("card was scanned but no result");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.callbackContext.error("card scan cancelled");
            } else {
                this.callbackContext.error(resultCode);
            }
        }
    }

    private JSONObject toJSONObject(CreditCard card) {
        JSONObject scanCard = new JSONObject();
        try {
            scanCard.put("cardType", card.getCardType());
            scanCard.put("redactedCardNumber", card.getRedactedCardNumber());
            scanCard.put("cardNumber", card.cardNumber);
            scanCard.put("expiryMonth", card.expiryMonth);
            scanCard.put("expiryYear", card.expiryYear);
            scanCard.put("cvv", card.cvv);
            scanCard.put("postalCode", card.postalCode);
            scanCard.put("cardholderName", card.cardholderName);
        } catch (JSONException e) {
            scanCard = null;
        }

        return scanCard;
    }

    private <T> T getConfiguration(JSONObject configurations, String name, T defaultValue) {
        if (configurations.has(name)) {
            try {
                return (T)configurations.get(name);
            } catch (JSONException ex) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
}
*/
