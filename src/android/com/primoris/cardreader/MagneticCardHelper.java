package com.primoris.cardreader;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.*;
import java.text.*;
import java.io.InputStream;

import org.apache.cordova.PluginResult;
import com.telpo.tps550.api.util.StringUtil;
import android.util.Base64;
import org.apache.cordova.CallbackContext;
import java.io.ByteArrayOutputStream;
import org.json.JSONObject;
import android.content.Context;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import android.content.BroadcastReceiver;
import com.telpo.tps550.api.reader.ReaderMonitor;
import com.telpo.tps550.api.reader.SmartCardReader;
import java.io.UnsupportedEncodingException;
import android.content.IntentFilter;
import com.telpo.tps550.api.reader.CardReader;
import android.os.BatteryManager;
import com.telpo.tps550.api.printer.NoPaperException;
import com.telpo.tps550.api.printer.OverHeatException;
import com.telpo.tps550.api.printer.ThermalPrinter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

public class MagneticCardHelper extends CordovaPlugin {

    Thread readThread;
    private CallbackContext connectionCallbackContext;

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
                        chipData = getCardDetails();
                        PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(chipData));
                        result.setKeepCallback(true);
                        connectionCallbackContext.sendPluginResult(result);
                        System.out.println("<<<<<<<<<<<<<<SMART CARD result chipData>>> " + chipData);
                    } else {
                        System.out.println("<<<<<<<<<<<Unknown>>>>>>>>>>>>>");
                    }
                } else {
                    System.out.println("<<<<<<<<<<<<<<<NO Card>>>>>>>>>>>>>>>>>>>");
                }
            }
        }

    };

    private final BroadcastReceiver mCR = mReceiver;

    @Override
    protected void pluginInitialize() {
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.connectionCallbackContext = null;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReaderMonitor.ACTION_ICC_PRESENT);
        webView.getContext().registerReceiver(mCR, filter);
    }

    private Activity activity = null;
    private static final int REQUEST_CARD_SCAN = 10;
    public String[] TracData = null;
    public Map<String, String> chipData = new HashMap();

    @Override
    public boolean execute(String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        this.activity = this.cordova.getActivity();
        boolean retValue = true;
        if (action.equals("open")) {
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        open();
                       // startMonitor();
                        callbackContext.success("Success open");
                    } catch (Exception ex) {
                        System.out.println("in teklpo exception" + ex);
                    }
                }
            });

        } else if (action.equals("startReading")) {
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        readThread = new ReadThread();
                        readThread.start();
                        startMonitor();
             /*           PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new JSONObject(chipData));
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);*/
                        /*PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, Arrays.toString(ans));
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);*/
                    } catch (Exception ex) {
                        System.out.println("in teklpo exception" + ex);
                    }
                }
            });

        } else if(action.equals("startMonitor")) {
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Map<String, String> result = startMonitor();
                        callbackContext.success(new JSONObject(result));
                    } catch (Exception ex) {
                        System.out.println("in teklpo exception" + ex);
                    }
                }
            });
        } else if(action.equals("readSmartCard")) {
            this.connectionCallbackContext = callbackContext;

            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new JSONObject(chipData));
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    } catch (Exception ex) {
                        System.out.println("in teklpo exception" + ex);
                    }
                }
            });
        } else if(action.equals("stop")) {
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        readThread.interrupt();
                        readThread = null;
                        close();
                     //   activity.unregisterReceiver(mCR);
                        ReaderMonitor.stopMonitor();
                        chipData = new HashMap();
                        callbackContext.success("STOP success");
                    } catch (Exception ex) {
                        System.out.println("in teklpo exception" + ex);
                    }
                }
            });
        } else if(action.equals("print")) {
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        int res = print(args.getString(0), args.getString(1), args.getString(2));
                        callbackContext.success(res);
                    } catch (Exception ex) {
                        System.out.println("in telpo exception" + ex);
                    }
                }
            });
        } else {
            retValue = false;
        }

        return retValue;
    }

    private class ReadThread extends Thread
    {
        @Override
        public void run()
        {
            MagneticCard.startReading();
            while (!Thread.interrupted()){
                try{
                    TracData = MagneticCard.check(1000);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, Arrays.toString(TracData));
                    result.setKeepCallback(true);
                    connectionCallbackContext.sendPluginResult(result);
                    MagneticCard.startReading();
                }catch (TelpoException e){}
            }
        }
    }

    public static void open() throws TelpoException {
        MagneticCard.open();
    }

    public static void close() throws TelpoException {
        MagneticCard.close();
    }

/*    public String[] startReading() throws TelpoException {
        MagneticCard.startReading();
        String[] arr = MagneticCard.check(100000);
        PluginResult result = new PluginResult(PluginResult.Status.OK, Arrays.toString(arr));
        result.setKeepCallback(true);
        connectionCallbackContext.sendPluginResult(result);
        return arr;
    }*/

    public Map<String, String> startMonitor() throws Exception {
        ReaderMonitor.setContext(this.activity);
        ReaderMonitor.startMonitor();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new JSONObject(chipData));
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        return chipData;
    }
//    private final BroadcastReceiver mReceiverCopy = mReceiver ;


    private Map<String, String> getCardDetails() {
        Map<String, String> cardAppIdentifiers = new HashMap();
        Map<String, String> result = new HashMap();
        Map<String, String> result1 = new HashMap();
        cardAppIdentifiers.put("A00000002501", "AMEX");
        cardAppIdentifiers.put("A0000000031010", "VISA");
        cardAppIdentifiers.put("A0000000041010", "MC");
        String selectCommandApdu = "00A40400";

        for(String key : cardAppIdentifiers.keySet()) {
            if(cardAppIdentifiers.get(key) == "AMEX") {
                String resp = sendApdu(selectCommandApdu + "06" + key + "00");
                result = checkSelectResponse(resp, cardAppIdentifiers.get(key));
                result1.putAll(result);
            } else {
                String resp = sendApdu(selectCommandApdu + "07" + key + "00");
                result = checkSelectResponse(resp, cardAppIdentifiers.get(key));
                result1.putAll(result);
            }
        }
        return result1;
    }

    private Map<String, String> checkSelectResponse(String resp, String cardType) {
        //Response APDU if File Not Found
        String fileResponseApdu = "6A82";
        //Get Response Command APDU
        String getCommandApdu = "00C000000000";
        //Get Processing options APDU
        String processingOptionsApdu = "80A80000028300";

        if(!resp.equals("6A82")) {
            return getCardDetailsHelper(resp, getCommandApdu, processingOptionsApdu, cardType);
        } else {
            Map<String, String> blankMap = new HashMap();
            return blankMap;
        }
    }

    private boolean checkValidResponse(String resApdu) {
        return resApdu.endsWith("9000");
    }

    private Map<String, String> getCardDetailsHelper(String response, String getCommandApdu, String processingOptionsApdu, String cardType) {
        String nBytes = response.substring(2);
        String getRespApdu = sendApdu(getCommandApdu + nBytes);
        if (checkValidResponse(getRespApdu)) {
            return callGetProcessingOptions(processingOptionsApdu, getCommandApdu, cardType);
        } else {
            throw new IllegalArgumentException("Invalid Parameter for command APDU");
        }
    }

    private Map<String, String> callGetProcessingOptions(String processingOptionsApdu, String getCommandApdu, String cardType) {
        String respApdu = sendApdu(processingOptionsApdu);
        String processingOptResp = sendApdu(getCommandApdu + respApdu.substring(2));
        if (checkValidResponse(processingOptResp)) {
            return getCommandAPDUParams(processingOptResp, cardType);
        } else {
            throw new IllegalArgumentException("Invalid Parameter for command APDU");
        }
    }

    private ArrayList<String> sliding(String resApdu) {
        ArrayList<String> resArr = new ArrayList<String>();
        char[] charArr = resApdu.toCharArray();

        for(int i = 0; i < resApdu.length(); i+=2) {
            resArr.add(charArr[i] + "" + charArr[i+1]);
        }
        return resArr;
    }

    private Map<String, String> getCommandAPDUParams(String resApdu, String cardType) {
        ArrayList<String> resArr = sliding(resApdu);
        Map<String, String> response;
        if(cardType.equals("VISA")) {
            String sfiStr1 = resArr.get(4);
            String p1 = resArr.get(5);
            String p2 = getParam2(sfiStr1);
            response = readCardDetails(p1, p2);
        } else if(cardType.equals("AMEX")) {
            String sfiStr2 = resArr.get(4);
            String p1_1 = resArr.get(5);
            String p2_1 = getParam2(sfiStr2);
            response = readCardDetails(p1_1, p2_1);
        } else if(cardType.equals("MC")) {
            int aflTagIndex = resArr.indexOf("94");
            String p1_2 = resArr.get(aflTagIndex + 3);
            String p2_2 = getParam2(resArr.get(aflTagIndex + 2));
            response = readCardDetails(p1_2, p2_2);
        } else {
            throw new IllegalArgumentException("Invalid card type");
        }
        return response;
    }


    private Map<String, String> readCardDetails(String param1, String param2) {
        String readRecordCommandApdu = "00B2" + param1 + param2;
        String readRecordResp = sendApdu(readRecordCommandApdu + "00");
        String cardDetailResp = sendApdu(readRecordCommandApdu + readRecordResp.substring(2));
        if(checkValidResponse(cardDetailResp)) {
            return getCardInfo(cardDetailResp);
        } else {
            throw new IllegalArgumentException("Invalid Parameter for command APDU");
        }

    }

    private Map<String, String> getCardInfo(String resApdu) {
        int startLoc = resApdu.indexOf("57");
        //Card account Holder name tag
        int cardHolderNameTagLoc = resApdu.indexOf("5F20");
        String cardNumAndExpiryDate = resApdu.substring(startLoc + 4, cardHolderNameTagLoc);
        //Expiry Date tag

        int expiryDateTagLoc = cardNumAndExpiryDate.indexOf("D");
        String cardNumber  = cardNumAndExpiryDate.substring(0, expiryDateTagLoc);

        String expiryDate = cardNumAndExpiryDate.charAt(expiryDateTagLoc + 3) + "" + cardNumAndExpiryDate.charAt(expiryDateTagLoc + 4) +
                cardNumAndExpiryDate.charAt(expiryDateTagLoc + 1) + cardNumAndExpiryDate.charAt(expiryDateTagLoc + 2);

        String nameInHexa = resApdu.substring(cardHolderNameTagLoc + 6, resApdu.lastIndexOf("20"));
        String cardHolderName = hexStringToAscii(nameInHexa);
        Map<String, String> cardInformation = new HashMap();
        cardInformation.put("cardNumber", cardNumber);
        cardInformation.put("cardHolderName", cardHolderName);
        cardInformation.put("expiryDate", expiryDate);
        return cardInformation;
    }

    private String hexStringToAscii(String hexValue) {
        StringBuilder output = new StringBuilder("");
        ArrayList<String> strArr = sliding(hexValue);

        for (int i = 0; i < strArr.size(); i ++) {
            String str = strArr.get(i);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString().trim();
    }


    private String getParam2(String sfiStr) {
        byte[] data = toByteArray(sfiStr);
        byte[] p2 = {(byte)(data[0] | 4)};
        return StringUtil.toHexString(p2);
    }

    private String sendApdu(String apdu) {
        byte[] apduArr = toByteArray(apdu);
        byte[] responseApdu = ReaderMonitor.transmit(apduArr);
        return StringUtil.toHexString(responseApdu);
    }

    private byte[] toByteArray(String hex) {
        String finedHex = hex.replaceAll("[^0-9A-Fa-f]", "");
        ArrayList<String> data = sliding(hex);
        ByteArrayOutputStream op = new ByteArrayOutputStream();
        for (int i = 0; i < data.size(); i++) {
            op.write((byte)Integer.parseInt(data.get(i),16));
        }
        return op.toByteArray();
    }

    /**
     * Plugin method for print functionality
     */
    private int print(String content, String sign, String logoPath) {
        if (getBatteryPercent() <= 5) {
            return -2;
        } else {
            return startPrinting(content, sign, logoPath);
        }
    }

    private int startPrinting(String content, String signImageDataUrl, String logoPath) {
        try {
            ThermalPrinter.start();
            ThermalPrinter.reset();
            ThermalPrinter.setLeftIndent(1);
            ThermalPrinter.setLineSpace(1);
            ThermalPrinter.setFontSize(2);
            ThermalPrinter.setGray(8);
            ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_MIDDLE);
            InputStream inputStream = null;

            inputStream = this.activity.getApplicationContext().getAssets().open(logoPath);
            Bitmap logoBitMap = BitmapFactory.decodeStream(inputStream);
            ThermalPrinter.printLogo(logoBitMap);

            ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_LEFT);
            ThermalPrinter.addString(content);
            ThermalPrinter.printString();
            String[] dataUrlArray = signImageDataUrl.split(",");
            byte[] decodedString = Base64.decode(dataUrlArray[1], Base64.DEFAULT);
            Bitmap bitMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ThermalPrinter.printLogo(bitMap);
            ThermalPrinter.walkPaper(100);
            return 0;
        } catch (NoPaperException ex) {
            ex.printStackTrace();
            return -1;
        } catch (OverHeatException ex) {
            ex.printStackTrace();
            return -3;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -4;
        } finally {
            ThermalPrinter.stop();
        }
    }

    private float getBatteryPercent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.activity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {}
        }, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (level / (float) 2.0) * 100;
    }
}
