package com.blq.ssnb.bleframe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blq.ssnb.bleframe.bodyfat.BleBodyFatHelper;
import com.blq.ssnb.bleframe.toothbrush.BleToothBrushHelper;
import com.blq.ssnb.bleframe.toothbrush.ToothBrushCmdUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import blq.ssnb.bleframe.listener.OnBleError;
import blq.ssnb.bleframe.listener.OnBleScan;
import blq.ssnb.bleframe.listener.OnBluetoothStateChange;
import blq.ssnb.bleframe.listener.OnCommandCallBack;
import blq.ssnb.bleframe.listener.OnGattStateChange;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    TextView showView;

    private ActionAdapter mActionAdapter;

    private BleToothBrushHelper mToothBrushHelper;
    private BleBodyFatHelper mBodyFatHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToothBrushHelper = new BleToothBrushHelper(this);
//        mBodyFatHelper = new BleBodyFatHelper(this);


        mToothBrushHelper.onCreate();
        mRecyclerView = findViewById(R.id.rv_action);
        showView = findViewById(R.id.tv_show_view);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        mActionAdapter = new ActionAdapter();
        mRecyclerView.setAdapter(mActionAdapter);
        mToothBrushHelper.setTextView(showView);

        check();
        updateAction(true);

        listener();
    }

    private void listener() {
        mToothBrushHelper.setOnBleScan(new OnBleScan() {
            @Override
            public void onScanResult(BluetoothDevice device) {
                showView.append("扫描到设备:" + device.getName() + "\n" + device.getAddress() + "\n");
            }

            @Override
            public void onScanTimeOut() {
                showView.append("扫描超时\n");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showView.append("扫描出错了:" + errorCode + ":" + errorMsg + "\n");
            }
        });

        mToothBrushHelper.setOnGattStateChange(new OnGattStateChange() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                showView.append("ble 蓝牙连接出错," + errorCode + ":" + errorMsg+"\n");
            }

            @Override
            public void onConnecting(String address) {
                showView.append("ble 蓝牙正在连接\n");
            }

            @Override
            public void onConnected(String address) {
                showView.append("ble 蓝牙已经连接\n");
            }

            @Override
            public void onDisconnecting(String address) {
                showView.append("ble 蓝牙正在断开\n");
            }

            @Override
            public void onDisconnected(String address) {
                showView.append("ble 蓝牙已经断开\n");
            }
        });

        mToothBrushHelper.setOnBluetoothStateChange(new OnBluetoothStateChange() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                showView.append("蓝牙连接出错," + errorCode + ":" + errorMsg+"\n");
            }

            @Override
            public void onTurningOn() {

                showView.append("蓝牙正在打开\n");
            }

            @Override
            public void onOn() {
                showView.append("蓝牙已经打开\n");
            }

            @Override
            public void onTurningOff() {
                showView.append("蓝牙正在关闭\n");
            }

            @Override
            public void onOff() {
                showView.append("蓝牙已经关闭\n");
            }
        });

        mToothBrushHelper.setOnCommandCallBack(new OnCommandCallBack() {
            @Override
            public void onGattRSSIResult(int rssi) {

            }

            @Override
            public void onSetMTUResult(int newSize) {

            }

            @Override
            public void onCommandResult(byte[] data) {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                showView.append("通信出错," + errorCode + ":" + errorMsg+"\n");
            }
        });

        mToothBrushHelper.setOnBleError(new OnBleError() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                showView.append("出错啦啦啦啦：" + errorCode + ":" + errorMsg+"\n");
            }
        });

    }

    private void updateAction(boolean isBrush) {
        mIsBrush = isBrush;
        mActionAdapter.replaceData(getData(isBrush));
    }

    private void check() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1111) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限通过", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean mIsBrush = true;

    private List<ActionBean> getData(boolean isBrush) {

        List<ActionBean> list = new ArrayList<>();
        list.add(new ActionBean().setTitle("切换连接设备:").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAction(!mIsBrush);
//                startActivity(new Intent(getBaseContext(),MainActivity.class));
            }
        }));
        list.add(new ActionBean()
                .setTitle("打开蓝牙")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.getBluetoothManager().openBluetooth();
                    }
                }));
        list.add(new ActionBean()
                .setTitle("关闭蓝牙")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.getBluetoothManager().closeBluetooth();
                    }
                }));
        list.add(new ActionBean()
                .setTitle("开始扫描")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.getBluetoothManager().startScanLE();
                    }
                }));
        list.add(new ActionBean()
                .setTitle("结束扫描")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.getBluetoothManager().stopScanLE();
                    }
                }));
        if (isBrush) {
            list.addAll(getBrushAction());
        } else {
            list.addAll(getBodyFatAction());
        }

        return list;
    }

    @Override
    protected void onDestroy() {
        mToothBrushHelper.onDestory();
        super.onDestroy();
    }

    private List<ActionBean> getBrushAction() {
        List<ActionBean> list = new ArrayList<>();
        list.add(new ActionBean()
                .setTitle("连接设备:牙刷-E7:63:C2:0C:92:54")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.connectionDevice("E7:63:C2:0C:92:54");
                    }
                }));
        list.add(new ActionBean()
                .setTitle("断开设备")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.disConnectionDevice();
                    }
                }));
        list.add(new ActionBean()
                .setTitle("发送数据:获取设备时间")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mToothBrushHelper.sendCommand(ToothBrushCmdUtil.getDeviceTime());
                    }
                }));
        list.add(new ActionBean()
                .setTitle("设置系统时间:2019-09-27 15:17:26")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar ca = Calendar.getInstance();
                        ca.setTimeInMillis(1569568646835L);
                        mToothBrushHelper.sendCommand(ToothBrushCmdUtil.setDeviceTime(ca));
                    }
                }));
        list.add(new ActionBean()
                .setTitle("设置系统当前时间")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar ca = Calendar.getInstance();
                        mToothBrushHelper.sendCommand(ToothBrushCmdUtil.setDeviceTime(ca));
                    }
                }));

        list.add(new ActionBean().setTitle("开始指导刷牙").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mToothBrushHelper.sendCommand(ToothBrushCmdUtil.BrushingGuidingProgress(1, 3));
            }
        }));

        list.add(new ActionBean().setTitle("结束指导刷牙").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToothBrushHelper.sendCommand(ToothBrushCmdUtil.BrushingGuidingProgress(0, 0));
            }
        }));
        list.add(new ActionBean().setTitle("设置刷牙档位2").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToothBrushHelper.sendCommand(ToothBrushCmdUtil.setToothBrushStatus(2));
            }
        }));
        list.add(new ActionBean().setTitle("设置刷牙档位4").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToothBrushHelper.sendCommand(ToothBrushCmdUtil.setToothBrushStatus(4));
            }
        }));

        list.add(new ActionBean().setTitle("获取历史数据").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToothBrushHelper.sendCommand(ToothBrushCmdUtil.getHistoryBrushingData());
            }
        }));


        return list;
    }

    private List<ActionBean> getBodyFatAction() {
        List<ActionBean> list = new ArrayList<>();
        list.add(new ActionBean()
                .setTitle("连接设备:体脂称-A4:C1:38:56:9B:FC")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mToothBrushHelper.connectionDevice("A4:C1:38:56:9B:FC");
                    }
                }));
        list.add(new ActionBean()
                .setTitle("")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }));
        list.add(new ActionBean()
                .setTitle("")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }));
        list.add(new ActionBean()
                .setTitle("")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }));
        list.add(new ActionBean()
                .setTitle("")
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }));

        return list;
    }
}
