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
import android.content.IntentFilter;
import java.io.UnsupportedEncodingException;
import com.telpo.tps550.api.reader.CardReader;
import android.os.BatteryManager;
import com.telpo.tps550.api.printer.NoPaperException;
import com.telpo.tps550.api.printer.OverHeatException;
import com.telpo.tps550.api.printer.ThermalPrinter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
    public boolean execute(String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.activity = this.cordova.getActivity();
        boolean retValue = true;
        if (action.equals("open")) {
            System.out.println("\n\n\n In Action == open \n\n\n\n");

            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        open();
                        callbackContext.success("Successssssss opeeeeenn");
                    } catch (Exception ex) {
                        System.out.println("in telpo exception");
                    }
                }
            });

        } else if (action.equals("startReading")) {
            System.out.println("\n\n\n In Action == startReading \n\n\n\n");
            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        String[] ans = startReading();
                        callbackContext.success(Arrays.toString(ans));
                    } catch (Exception ex) {
                        System.out.println("in telpo exception");
                    }
                }
            });

        } else if(action.equals("startMonitor")) {
            System.out.println("\n\n\n In Action == startMonitor \n\n\n\n");

            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Map<String, String> result = startMonitor();
                        callbackContext.success(new JSONObject(result));
                        System.out.println("AFTER SENDING SUCCESS >>>>>>> " + result);
                        //activity.unregisterReceiver(mReceiverCopy);
                    } catch (Exception ex) {
                        System.out.println("in telpo exception");
                    }
                }
            });
        } else if(action.equals("stop")) {
            System.out.println("\n\n\n In Action == stop \n\n\n\n");

            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        //Stop Smart card reader
                        activity.unregisterReceiver(mReceiverCopy);
                        ReaderMonitor.stopMonitor();
                        //Stop mag card reader
                        close();
//                        Map<String, String> result = startMonitor();
                        callbackContext.success("STOP success");
                    } catch (Exception ex) {
                        System.out.println("in telpo exception");
                    }
                }
            });
        } else if(action.equals("print")) {
            System.out.println("\n\n\n In Action == print \n\n\n\n");

            this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Print agrs >>>>>>>>>>>>>>>>> "+args);
                        int res = print(args.getString(0), args.getString(1));
                        callbackContext.success(res);
                    } catch (Exception ex) {
                        System.out.println("in telpo exception");
                    }
                }
            });
        } else {
            retValue = false;
        }

        return retValue;
    }

    public static void open() throws TelpoException {
        MagneticCard.open();
    }

    public static void close() throws TelpoException {
        MagneticCard.close();
    }

    public static String[] startReading() throws TelpoException {
        MagneticCard.startReading();
        String[] arr = MagneticCard.check(10000);
        return arr;
    }



    public Map<String, String> chipData = new HashMap();

    public Map<String, String> startMonitor() throws TelpoException {
        ReaderMonitor.setContext(this.activity);
        ReaderMonitor.startMonitor();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ReaderMonitor.ACTION_ICC_PRESENT);

        System.out.println("<<<<<<<<<< Before register ??? >> " + chipData);

        this.activity.registerReceiver(mReceiver, filter);

        try {
            //TODO -- DONT USE Thread.sleep()
            Thread.sleep(4000);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("<<<<<<<<<< After register ??? >> " + chipData);
        return chipData;
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
                        chipData = getCardDetails();
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

    private final BroadcastReceiver mReceiverCopy = mReceiver ;


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
                System.out.println("<<<<<<<<<<<<>>>AMEX>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                String resp = sendApdu(selectCommandApdu + "06" + key + "00");
                System.out.println("<<<<<<<<<<<<>>>AMEX>>>RESP>>>>>>>>>>>>>>>>>>>>>>>>" + resp);
                result = checkSelectResponse(resp, cardAppIdentifiers.get(key));
                result1.putAll(result);
                System.out.println("<<<<<<<THIS IS THE ANS <<AMEX<<<>>>>>" + result);
            } else {
                System.out.println("<<<<<<<<<<<<>>>>>OTHER THEN AMEX>>>>>>>>>>>>>>>>>>>>>>>>>");
                String resp = sendApdu(selectCommandApdu + "07" + key + "00");
                System.out.println("<<<<<<<<<<<<>>>>>OTHER THEN AMEX>>>>>resp>>>>>>>>>>>>>>>>>>>>" + resp);
                result = checkSelectResponse(resp, cardAppIdentifiers.get(key));
                result1.putAll(result);
                System.out.println("<<<<<<<THIS IS THE ANS <<< other then amex result<<>>>>>" + result);
                System.out.println("<<<<<<<THIS IS THE ANS <<< other then amex result1<<>>>>>" + result1);
            }
        }
        return result1;
    }

    private Map<String, String> checkSelectResponse(String resp, String cardType) {

        System.out.println("<<<<<<<<<<<<in >>>checkSelectResponse>>>>>>>>>>>card type === " + cardType + ">>>>>>>>>>>>>>>>");
        //Response APDU if File Not Found
        String fileResponseApdu = "6A82";
        //Get Response Command APDU
        String getCommandApdu = "00C000000000";
        //Get Processing options APDU
        String processingOptionsApdu = "80A80000028300";

        if(!resp.equals("6A82")) {
            System.out.println("----------------in!resp.equal6A82) ----------------");
            return getCardDetailsHelper(resp, getCommandApdu, processingOptionsApdu, cardType);
        } else {
            System.out.println("---------------- else case resp == " + resp +" ----------------");
            Map<String, String> blankMap = new HashMap();
            return blankMap;
        }
    }

    private boolean checkValidResponse(String resApdu) {
        System.out.println("in chack valid respons enad stsing is ===" + resApdu);
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
        System.out.println("------IN readCardDetails FUNCTION ---param 1 = "+ param1+"--------param 2 === "+ param2+"---------------");
        if(checkValidResponse(cardDetailResp)) {
            System.out.println("+++++++++++check valid response if case and card deaaa =   "+ cardDetailResp+" +++++++++++");
            return getCardInfo(cardDetailResp);
        }
        else {
            System.out.println("---------param 1 = "+ param1+"--------param 2 === "+ param2+"---------------");
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

        System.out.println("----------- output.toString().trim()== " + output.toString().trim());
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
    private int print(String content, String sign) {
        if (getBatteryPercent() <= 5) {
            return -2;
        } else {
            return startPrinting(content, sign);
        }
    }

    private int startPrinting(String content, String sign) {
        System.out.println("\n\n STRING sign >>>>>>>>>>>>>>>> "+sign);
        try {
            ThermalPrinter.start();
            ThermalPrinter.reset();
            ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_LEFT);
            ThermalPrinter.setLeftIndent(1);
            ThermalPrinter.setLineSpace(1);
            ThermalPrinter.setFontSize(2);
            ThermalPrinter.setGray(8);

            InputStream inputStream = null;

            for (int i=0; i<this.activity.getApplicationContext().getAssets().list("www/assets").length; i++) {
                // Get filename of file or directory
                String filename = this.activity.getApplicationContext().getAssets().list("www/assets")[i];
                System.out.println("\n\n getAssets >>>>>>>>>>>>>>>> "+filename);
            }

            inputStream = this.activity.getApplicationContext().getAssets().open("www/assets/primoris.png");
            Bitmap logoBitMap = BitmapFactory.decodeStream(inputStream);
            ThermalPrinter.printLogo(logoBitMap);

            ThermalPrinter.addString(content);
            ThermalPrinter.printString();
            String[] sign1 = sign.split(",");
            byte[] decodedString = Base64.decode(sign1[1], Base64.DEFAULT);
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
