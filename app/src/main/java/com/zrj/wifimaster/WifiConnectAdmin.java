package com.zrj.wifimaster;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by a on 2017/6/1.
 */

/****************************
 * wifi管理类
 ***************************************/
public class WifiConnectAdmin {

    public static final int PASS_NONE = 0;
    public static final int TYPE_WEP = 1;
    public static final int TYPE_WPA = 2;

    private List<WifiConfiguration> mConfigurations;//已配置（保存）的 wifi 网络列表
    private WifiManager mManager;
    private WifiInfo mWifiInfo;   //已连接的 wifi 信息
    private List<ScanResult> mScanResults; //扫描结果

    public WifiConnectAdmin(Context context) {
        mManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mManager.getConnectionInfo();
    }

    //打开wifi
    public void openWifi() {
        if (!mManager.isWifiEnabled()) {
            mManager.setWifiEnabled(true);
        }
    }

    //关闭wifi
    public void closeWifi() {
        if (mManager.isWifiEnabled()) {
            mManager.setWifiEnabled(false);
        }
    }

    //开始扫描
    public void startScan() {
        mManager.startScan();
        mScanResults = mManager.getScanResults();
        mConfigurations = mManager.getConfiguredNetworks();
    }


    //得到原始扫描结果
    public List<ScanResult> getScanResults() {
        if (mScanResults == null) {
            mScanResults = mManager.getScanResults();
        }
        return mScanResults;
    }

    //返回已配置（保存）的 wifi 网络
    public List<WifiConfiguration> getConfigurations() {
        return mConfigurations;
    }

    //获取MAC地址
    public String getMacAdress() {
        return (mWifiInfo == null) ? null : mWifiInfo.getMacAddress();
    }

    //得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? null : mWifiInfo.getBSSID();
    }

    //得到接入点的SSID
    public String getSSID() {
        return (mWifiInfo == null) ? null : mWifiInfo.getSSID();
    }

    //得到IP地址
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    //得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    //得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? null : mWifiInfo.toString();
    }

    /**
     * 创建 wifi config
     * @param SSID  用于创建config的SSID
     * @param password  密码
     * @param passType 密码类型
     */
    public WifiConfiguration createConfig(String SSID, String password, int passType) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        switch (passType) {
            case PASS_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case TYPE_WEP:
                config.wepKeys[0] = "\"" + password + "\"";
                config.wepTxKeyIndex = 0;
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                break;
            case TYPE_WPA:
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return config;
    }

    //连接到指定 wifi 网络
    public void connect(WifiConfiguration config) {
        int netId = mManager.addNetwork(config);
        Log.e(WifiConnectAdmin.class.getName(), "netId = " + netId);
        boolean enable = mManager.enableNetwork(netId, true);
        mManager.reconnect();
    }

    /**
     * 根据 ScanResult 来获取密码类型
     */
    public int getPassType(ScanResult result) {
        if (result.capabilities.contains("PSK")) {
            return TYPE_WPA;
        }
        if (result.capabilities.contains("WEP")) {
            return TYPE_WEP;
        }
        return PASS_NONE;
    }


}
