package com.primoris.cardreader;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import com.telpo.tps550.api.util.StringUtil;
import javax.xml.bind.DatatypeConverter;
import org.apache.cordova.CallbackContext;
import android.content.Context;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
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
import com.telpo.tps550.api.reader.SmartCardReader;
import java.io.UnsupportedEncodingException;
import android.content.IntentFilter;
import com.telpo.tps550.api.reader.CardReader;


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
                        getCardDetails();

                    } else {
                        System.out.println("<<<<<<<<<<<Unknown>>>>>>>>>>>>>");
                    }
                } else {
                    System.out.println("<<<<<<<<<<<<<<<NO Card>>>>>>>>>>>>>>>>>>>");
                }
            }
        }

    };


    private void getCardDetails() {
        Map cardAppIdentifiers = new HashMap();
        cardAppIdentifiers.put("A00000002501", "AMEX");
        cardAppIdentifiers.put("A0000000031010", "VISA");
        cardAppIdentifiers.put("A0000000041010", "MC");

        String selectCommandApdu = "00A40400";

        for(String key: cardAppIdentifiers.keySet()) {
            if(cardAppIdentifiers.get(key) == "AMEX") {
                String resp = sendApdu(selectCommandApdu + "06" + key + "00");
                Map<String, String> ans = checkSelectResponse(resp, cardType);
                System.out.println("<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + ans);
                return ans;
            } else {
                String resp = sendApdu(selectCommandApdu + "07" + cardAID + "00");
                Map<String, String> ans = checkSelectResponse(resp, cardType);
                System.out.println("<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + ans);
                return ans;
            }
        }




    }

    private Map<String, String> checkSelectResponse(String resp, String cardType) {
        //Response APDU if File Not Found
        String fileResponseApdu = "6A82";
        //Get Response Command APDU
        String getCommandApdu = "00C000000000";
        //Get Processing options APDU
        String processingOptionsApdu = "80A80000028300";

        switch(resp) {
            case "6A82": new HashMap();
                break;
            default: getCardDetailsHelper(response, getCommandApdu, processingOptionsApdu, cardType);
        }


    }

    private boolean checkValidResponse(resApdu: String) = {
        resApdu.endsWith("9000");
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
        String processingOptResp = sendApdu(getCommandApdu + respApdu.substring(TWO));
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

        switch(cardType) {
            case "VISA" | "AMEX":
                String sfiStr = resArr.get(4);
                String param1 = resArr.get(5);
                String param2 = getParam2(sfiStr);
                return readCardDetails(param1, param2);
                break;
            case "MC":
                int aflTagIndex = resArr.indexOf("94");
                String param1 = resArr.get(aflTagIndex + 3);
                String param2 = getParam2(resArr.get(aflTagIndex + 2));
                return readCardDetails(param1, param2);
                break;
            default: throw new IllegalArgumentException("Invalid card type");
        }
    }


    private Map<String, String> readCardDetails(String param1, String param1) {
        String readRecordCommandApdu = "00B2" + param1 + param2;
        String readRecordResp = sendApdu(readRecordCommandApdu + "00");
        String cardDetailResp = sendApdu(readRecordCommandApdu + readRecordResp.substring(TWO));
        if(checkValidResponse(cardDetailResp)) {
            return getCardInfo(cardDetailResp);
        }
        else {
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
        String cardNumber  = cardNumAndExpiryDate.substring(ZERO, expiryDateTagLoc);

        String expiryDate = cardNumAndExpiryDate(expiryDateTagLoc + 3).toString + cardNumAndExpiryDate(expiryDateTagLoc + 4) +
                cardNumAndExpiryDate(expiryDateTagLoc + 1) + cardNumAndExpiryDate(expiryDateTagLoc + 2);

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
        for (int i = 0; i < hexValue.length(); i += 2) {
            String str = hexValue.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }


    private String getParam2(String sfiStr) {
        byte[] sfiByte = sfiStr.getBytes();
        byte[] p2 = {(byte)(sfiByte[0] | 4)};
        return StringUtil.toHexString(p2);
    }



    private String sendApdu(String apdu) {
        byte[] apduArr = toByteArray(apdu);
        return ReaderMonitor.transmit(apduArr);
    }

    private byte[] toByteArray(String hex) {
        String finedHex = hex.replaceAll("[^0-9A-Fa-f]", "");
        return DatatypeConverter.parseHexBinary(finedHex);
    }



}
