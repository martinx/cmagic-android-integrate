/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aicaima.client.android.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>A utility class which helps ease integration with Barcode Scanner via {@link android.content.Intent}s. This is a simple
 * way to invoke barcode scanning and receive the result, without any need to integrate, modify, or learn the
 * project's source code.</p>
 * <p/>
 * <h2>Initiating a barcode scan</h2>
 * <p/>
 * <p>To integrate, create an instance of {@code IntentIntegrator} and call {@link #initiateScan()} and wait
 * for the result in your app.</p>
 * <p/>
 * <p>It does require that the Barcode Scanner (or work-alike) application is installed. The
 * {@link #initiateScan()} method will prompt the user to download the application, if needed.</p>
 * <p/>
 * <p>There are a few steps to using this integration. First, your {@link android.app.Activity} must implement
 * the method {@link android.app.Activity#onActivityResult(int, int, android.content.Intent)} and include a line of code like this:</p>
 * <p/>
 * <pre>{@code
 * public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 *   IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 *   if (scanResult != null) {
 *     // handle scan result
 *   }
 *   // else continue with any other code you need in the method
 *   ...
 * }
 * }</pre>
 * <p/>
 * <p>This is where you will handle a scan result.</p>
 * <p/>
 * <p>Second, just call this in response to a user action somewhere to begin the scan process:</p>
 * <p/>
 * <pre>{@code
 * IntentIntegrator integrator = new IntentIntegrator(yourActivity);
 * integrator.initiateScan();
 * }</pre>
 * <p/>
 * <p>Note that {@link #initiateScan()} returns an {@link android.app.AlertDialog} which is non-null if the
 * user was prompted to download the application. This lets the calling app potentially manage the dialog.
 * In particular, ideally, the app dismisses the dialog if it's still active in its {@link android.app.Activity#onPause()}
 * method.</p>
 * <p/>
 * <p>You can use {@link #setTitle(String)} to customize the title of this download prompt dialog (or, use
 * {@link #setTitleByID(int)} to set the title by string resource ID.) Likewise, the prompt message, and
 * yes/no button labels can be changed.</p>
 * <p/>
 * <p>By default, this will only allow applications that are known to respond to this intent correctly
 * do so. The apps that are allowed to response can be set with {@link #setTargetApplications(java.util.Collection)}.
 * For example, set to {@link #TARGET_BARCODE_SCANNER_ONLY} to only target the Barcode Scanner app itself.</p>
 * <p/>
 * <h2>Sharing text via barcode</h2>
 * <p/>
 * <p>To share text, encoded as a QR Code on-screen, similarly, see {@link #shareText(CharSequence)}.</p>
 * <p/>
 * <p>Some code, particularly download integration, was contributed from the Anobiit application.</p>
 *
 * @author Sean Owen
 * @author Fred Lin
 * @author Isaac Potoczny-Jones
 * @author Brad Drehmer
 * @author gcstang
 * @author Martin Xu
 */
public class IntentIntegrator {

    public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16 bits
    private static final String TAG = IntentIntegrator.class.getSimpleName();

    public static final String SCAN_RESULT = "SCAN_RESULT";
    public static final String SCAN_RESULT_FORMAT = "SCAN_RESULT_FORMAT";
    public static final String SCAN_RESULT_BYTES = "SCAN_RESULT_BYTES";
    public static final String DECODE_RESULT_ITEM_ID = "DECODE_RESULT_ITEM_ID";
    public static final String DECODE_RESULT_TITLE = "DECODE_RESULT_TITLE";
    public static final String DECODE_RESULT_CONTENT = "DECODE_RESULT_CONTENT";
    public static final String DECODE_RESULT_CODE = "DECODE_RESULT_CODE";

    public static final String DEFAULT_TITLE = "安装彩码快拍?";
    public static final String DEFAULT_MESSAGE = "此程序需要彩码快拍，是否安装?";
    public static final String DEFAULT_YES = "是";
    public static final String DEFAULT_NO = "否";

    private static final String BS_PACKAGE = "com.aicaima.app.android";


    // supported barcode formats
    public static final Collection<String> PRODUCT_CODE_TYPES = list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "RSS_14");
    public static final Collection<String> ONE_D_CODE_TYPES = list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "CODE_39", "CODE_93", "CODE_128", "ITF", "RSS_14", "RSS_EXPANDED");
    public static final Collection<String> QR_CODE_TYPES = Collections.singleton("QR_CODE");
    public static final Collection<String> DATA_MATRIX_TYPES = Collections.singleton("DATA_MATRIX");
    public static final Collection<String> COLOR_CODE_TYPES = Collections.singleton("COLOR_CODE");

    public static final Collection<String> ALL_CODE_TYPES = null;

    public static final Collection<String> TARGET_BARCODE_SCANNER_ONLY = Collections.singleton(BS_PACKAGE);
    public static final Collection<String> TARGET_ALL_KNOWN = list(
            BS_PACKAGE
    );

    private final Activity activity;
    private String title;
    private String message;
    private String buttonYes;
    private String buttonNo;
    private String appKey;
    private String secretKey;
    private String promptMessage;
    private boolean  updateFlag;
    private Collection<String> targetApplications;

    public IntentIntegrator(Activity activity) {
        this.activity = activity;
        title = DEFAULT_TITLE;
        message = DEFAULT_MESSAGE;
        buttonYes = DEFAULT_YES;
        buttonNo = DEFAULT_NO;
        targetApplications = TARGET_ALL_KNOWN;
    }

    public boolean isUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(boolean updateFlag) {
        this.updateFlag = updateFlag;
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    public void setPromptMessage(String promptMessage) {
        this.promptMessage = promptMessage;
    }

    public void setPromptMessageByID(int promptMessageID) {
        this.promptMessage = activity.getString(promptMessageID);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleByID(int titleID) {
        title = activity.getString(titleID);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageByID(int messageID) {
        message = activity.getString(messageID);
    }

    public String getButtonYes() {
        return buttonYes;
    }

    public void setButtonYes(String buttonYes) {
        this.buttonYes = buttonYes;
    }

    public void setButtonYesByID(int buttonYesID) {
        buttonYes = activity.getString(buttonYesID);
    }

    public String getButtonNo() {
        return buttonNo;
    }

    public void setButtonNo(String buttonNo) {
        this.buttonNo = buttonNo;
    }

    public void setButtonNoByID(int buttonNoID) {
        buttonNo = activity.getString(buttonNoID);
    }

    public Collection<String> getTargetApplications() {
        return targetApplications;
    }

    public void setTargetApplications(Collection<String> targetApplications) {
        this.targetApplications = targetApplications;
    }

    public void setSingleTargetApplication(String targetApplication) {
        this.targetApplications = Collections.singleton(targetApplication);
    }

    public AlertDialog initiateScan() {
        return initiateScan(COLOR_CODE_TYPES);
    }

    public AlertDialog initiateScan(Collection<String> desiredBarcodeFormats) {
//        Intent intentScan = activity.getIntent();
        Intent intentScan = new Intent(BS_PACKAGE + ".SCAN");
        intentScan.setAction(BS_PACKAGE + ".SCAN");
        //new Intent(BS_PACKAGE + ".SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        // check which types of codes to scan for
        if (desiredBarcodeFormats != null) {
            StringBuilder joinedByComma = new StringBuilder();
            for (String format : desiredBarcodeFormats) {
                if (joinedByComma.length() > 0) {
                    joinedByComma.append(',');
                }
                joinedByComma.append(format);
            }
            intentScan.putExtra("SCAN_FORMATS", joinedByComma.toString());
        }


        intentScan.putExtra("PROMPT_MESSAGE", this.getPromptMessage());
        intentScan.putExtra("APP_KEY", this.getAppKey());
        intentScan.putExtra("SECRET_KEY", this.getSecretKey());
        intentScan.putExtra("UPDATE_FLAG", this.isUpdateFlag());

        String targetAppPackage = findTargetAppPackage(intentScan);

        if (targetAppPackage == null) {
            return showDownloadDialog();
        }
        intentScan.setPackage(targetAppPackage);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intentScan, REQUEST_CODE);
        return null;
    }


    protected void startActivityForResult(Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }

    private String findTargetAppPackage(Intent intent) {
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (availableApps != null) {
            for (ResolveInfo availableApp : availableApps) {
                String packageName = availableApp.activityInfo.packageName;
                if (targetApplications.contains(packageName)) {
                    return packageName;
                }
            }
        }
        return null;
    }

    //todo:
    private AlertDialog showDownloadDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://details?id=" + BS_PACKAGE);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    Log.w(TAG, "Android Market is not installed; cannot install Barcode Scanner");
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }


    public static IntentResult parseActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "REQUEST_CODE:" + REQUEST_CODE);
        Log.d(TAG, "RESULT_OK:" + (resultCode == Activity.RESULT_OK));
        if (resultCode == Activity.RESULT_OK) {
            String scanResult = intent.getStringExtra(SCAN_RESULT);
            String formatName = intent.getStringExtra(SCAN_RESULT_FORMAT);
//            byte[] rawBytes = intent.getByteArrayExtra(SCAN_RESULT_BYTES);
            String decodeResultCode = intent.getStringExtra(DECODE_RESULT_CODE);
            String decodeResultItemId = intent.getStringExtra(DECODE_RESULT_ITEM_ID);
            String decodeResultTitle = intent.getStringExtra(DECODE_RESULT_TITLE);
            String decodeResultContent = intent.getStringExtra(DECODE_RESULT_CONTENT);
            return new IntentResult(scanResult, decodeResultCode, decodeResultItemId, decodeResultTitle, decodeResultContent, formatName, null);
        } else {
            return null;
        }
    }


    public void shareText(CharSequence text) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(BS_PACKAGE + ".ENCODE");
        intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
        intent.putExtra("ENCODE_DATA", text);
        String targetAppPackage = findTargetAppPackage(intent);
        if (targetAppPackage == null) {
            showDownloadDialog();
        } else {
            intent.setPackage(targetAppPackage);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            activity.startActivity(intent);
        }
    }

    private static Collection<String> list(String... values) {
        return Collections.unmodifiableCollection(Arrays.asList(values));
    }

}
