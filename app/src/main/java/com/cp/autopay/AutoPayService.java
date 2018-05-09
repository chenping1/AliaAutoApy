package com.cp.autopay;

import android.accessibilityservice.AccessibilityService;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * author: cp48876
 * date: 2018/1/23.
 * email: cp48876@ly.com
 */

public class AutoPayService extends AccessibilityService {


    private static final String[] payButton = {"立即付款"};
    private static final String[] password = {"1", "1", "1", "1", "1", "1"};

    boolean isRun = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                if (event == null || event.getSource() == null || getRootInActiveWindow() == null) {
                    return;
                }

                if (event.getPackageName() == null || false == event.getPackageName().equals("com.eg.android.AlipayGphone")) {
                    return;
                }

                clickButtonByName(event.getSource(), payButton);

                String inputPassword = "请输入支付密码";
                List<AccessibilityNodeInfo> okNodes = event.getSource().findAccessibilityNodeInfosByText(inputPassword);
                if (okNodes != null && okNodes.size() > 0) {
                    if (isRun) {
                        return;
                    } else {
                        isRun = true;
                    }
                    for (AccessibilityNodeInfo node : okNodes) {
                        CharSequence charSequence = node.getText();
                        if (!TextUtils.isEmpty(charSequence) && charSequence.toString().equals(inputPassword)) {
                            clickButtonByNameRoot(event.getSource(), password);
                            isRun = false;
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TAJ_STAR", e.toString());
            }
        }
        isRun = false;
    }


    @Override
    public void onInterrupt() {

    }

    private void clickButtonByNameRoot(AccessibilityNodeInfo accessibilityNodeInfo, String[] btnNames) {
        for (String btnName : btnNames) {
            List<AccessibilityNodeInfo> okNodes = accessibilityNodeInfo.findAccessibilityNodeInfosByText(btnName);
            if (okNodes != null && okNodes.size() > 0) {
                for (AccessibilityNodeInfo node : okNodes) {
                    CharSequence charSequence = node.getText();
                    if (TextUtils.isEmpty(charSequence) || !charSequence.toString().equals(btnName)) {
                        continue;
                    }

                    if ((node.getClassName().equals("android.widget.Button") ||
                            node.getClassName().equals("android.widget.TextView")) && node.isEnabled()) {
                        perforGlobalClick(node);
                    }
                }
            }
        }
    }

    private void clickButtonByName(AccessibilityNodeInfo accessibilityNodeInfo, String[] btnNames) {
        for (String btnName : btnNames) {
            List<AccessibilityNodeInfo> okNodes = accessibilityNodeInfo.findAccessibilityNodeInfosByText(btnName);
            if (okNodes != null && okNodes.size() > 0) {
                for (AccessibilityNodeInfo node : okNodes) {
                    CharSequence charSequence = node.getText();
                    if (TextUtils.isEmpty(charSequence) || !charSequence.toString().equals(btnName)) {
                        continue;
                    }

                    if ((node.getClassName().equals("android.widget.Button") ||
                            node.getClassName().equals("android.widget.TextView")) && node.isEnabled()) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }
    }


    /**
     * 点击某个视图
     */
    public static void perforGlobalClick(AccessibilityNodeInfo info) {
        Rect rect = new Rect();
        info.getBoundsInScreen(rect);
        perforGlobalClick(rect.centerX(), rect.centerY());
    }

    public static void perforGlobalClick(int x, int y) {
        execShellCmd("input tap " + x + " " + y);
    }

    /**
     * 执行shell命令
     *
     * @param cmd
     */
    public static void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
//            process.waitFor();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    // To check if service is enabled
    public static boolean isAccessibilitySettingsOn(Context context) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + AutoPayService.class.getName();
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        } else {
            // Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}