package com.zrj.wifimaster;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WifiConnectAdmin connectAdmin;
    private ListView wifiList;
    private List<String> mSSIDs = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<ScanResult> mScanResults;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btOpenWifi = (Button) findViewById(R.id.open_wifi);
        Button btCloseWifi = (Button) findViewById(R.id.close_wifi);
        Button btScanWifi = (Button) findViewById(R.id.scan_wifi);
        wifiList = (ListView) findViewById(R.id.wifi_list);
        TextView wifiText = (TextView) findViewById(R.id.wifi_text);
        connectAdmin = new WifiConnectAdmin(this);
        mScanResults = connectAdmin.getScanResults();
        refreshSSIDs();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mSSIDs);
        btOpenWifi.setOnClickListener(this);
        btCloseWifi.setOnClickListener(this);
        btScanWifi.setOnClickListener(this);
        wifiList.setAdapter(adapter);
        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String SSID = mSSIDs.get(position);
                if (connectAdmin.getPassType(getScanResult(SSID)) == WifiConnectAdmin.PASS_NONE) {
                    WifiConfiguration config = connectAdmin.createConfig(SSID, null, WifiConnectAdmin.PASS_NONE);
                    connectAdmin.connect(config);
                    Log.e(getLocalClassName(),SSID+" \"无密码\"");
                }else{
                    final EditText editText = new EditText(MainActivity.this);
                    showPassDialog(editText,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    WifiConfiguration config = connectAdmin.createConfig(SSID,editText.getText().toString(),
                                            connectAdmin.getPassType(getScanResult(SSID)));
                                    connectAdmin.connect(config);
                                }
                            }).start();
                            dismissDialog();
                            Log.e(getLocalClassName(),SSID+" \"有密码\"");
                        }
                    });
                }
                Toast.makeText(MainActivity.this, mSSIDs.get(position), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_wifi:
                connectAdmin.openWifi();
                break;
            case R.id.close_wifi:
                connectAdmin.closeWifi();
                break;
            case R.id.scan_wifi:
                refreshSSIDs();
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }


    //刷新 wifi 列表数据
    private void refreshSSIDs() {
        mSSIDs.clear();
        if (connectAdmin != null) {
            connectAdmin.startScan();
            mScanResults = connectAdmin.getScanResults();
        }
        if (mScanResults != null) {
            for (ScanResult scanResult : mScanResults) {
                if (!isRepeated(scanResult.SSID)) {
                    mSSIDs.add(scanResult.SSID);
                }
            }
        }
    }

    //检查是否有重复
    private boolean isRepeated(String SSID) {
        for (int i = mSSIDs.size() - 1; i >= 0; i--) {
            if (mSSIDs.get(i).equals(SSID)) {
                return true;
            }
        }
        return false;
    }

    //根据 SSID 获取相对应得 ScanResult
    private ScanResult getScanResult(String SSID) {
        for (ScanResult result : mScanResults) {
            if (result.SSID.equals(SSID)) {
                return result;
            }
        }
        return null;
    }

    //输入密码 dialog
    private void showPassDialog(EditText editText,DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入密码")
                .setView(editText)
                .setPositiveButton("确定", listener)
                .setCancelable(true);
        mDialog = builder.show();
    }

    //隐藏对话框
    private void dismissDialog(){
        if (mDialog!=null){
            mDialog.dismiss();
        }
    }

}
