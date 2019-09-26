package blq.ssnb.blutoothblefragment;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.ref.WeakReference;

import blq.ssnb.blutoothblefragment.inf.IBleDeviceRuler;
import blq.ssnb.blutoothblefragment.inf.IServiceBinder;
import blq.ssnb.blutoothblefragment.listener.BleScanCallBack;
import blq.ssnb.snbutil.SnbLog;


/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      具体的设备的操作帮助类
 * ================================================
 * </pre>
 */
public abstract class AbsBleDeviceHelper {

    // <editor-fold defaultstate="collapsed" desc="蓝牙管理器相关">
    private WeakReference<Activity> mActivityWeakReference;
    private SnbBluetoothManager mBluetoothManager;
    private SnbBluetoothManager.OnBLEScanCallBack mOnBLEScanCallBack;

    private BleScanCallBack mScanCallBack;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="和蓝牙服务相关">
    private ServiceConnection mServiceConnection;
    private IServiceBinder mBinder;
    private IBleDeviceRuler mDeviceRuler;
    private BleReceiver mBleReceiver;
    // </editor-fold>

    public AbsBleDeviceHelper(Activity activity) {
        mActivityWeakReference = new WeakReference<>(activity);

        mOnBLEScanCallBack = new SnbBluetoothManager.OnBLEScanCallBack() {
            @Override
            public void onScanResult(ScanResult result) {
                if (result != null && getDeviceRuler() != null) {
                    if (getDeviceRuler().isDevice(result)) {

                        BluetoothDevice device = result.getDevice();
                        SnbLog.se(Constant.LOG_TAG, getLogStr("扫描到符合的设备:" + device.toString()));
                        if (mScanCallBack != null) {
                            mScanCallBack.onScanResult(device);
                        }
                    }
                }
            }

            @Override
            public void onScanFail(int errorCode) {
                SnbLog.se(Constant.LOG_TAG, getLogStr("扫描失败：" + errorCode));
                if (mScanCallBack != null) {
                    mScanCallBack.onError(Constant.ErrorInfo.BLE_SCAN_FAIL_CODE, Constant.ErrorInfo.BLE_SCAN_FAIL_MSG);
                }
            }
        };

        mBluetoothManager = SnbBluetoothManager.singleton();

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof IServiceBinder) {
                    mBinder = (IServiceBinder) service;
                    SnbLog.se(Constant.LOG_TAG, getLogStr("服务连接：" + name));

                } else {
                    SnbLog.se(Constant.LOG_TAG, getLogStr("服务连接-但是不是IServiceBinder对象" + name));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                SnbLog.se(Constant.LOG_TAG, getLogStr("服务断开" + name));
                //断开服务的时候需要将连接断开并且置空
                if (mBinder != null) {
                    mBinder.unConnectDevice(lastConnectAddress);
                    // TODO: 2019-09-27 这里的lastaddress 还要考录如何处理
                    mBinder = null;
                }
            }
        };
        mBleReceiver = initBleReceiver();
    }

    // <editor-fold defaultstate="collapsed" desc="蓝牙基本相关-扫描回调等">

    /**
     * 蓝牙一些判断和一些开启关闭等操作都由他来完成
     */
    public SnbBluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    /**
     * 获得 设备的规则对象
     */
    protected IBleDeviceRuler getDeviceRuler() {
        if (mDeviceRuler == null) {
            mDeviceRuler = initDeviceRuler();
        }
        return mDeviceRuler;
    }

    /**
     * 初始化设备的加载规则
     *
     * @return
     */
    protected abstract IBleDeviceRuler initDeviceRuler();

    /**
     * 设置扫描的回调
     */
    public void setScanCallBack(BleScanCallBack callBack) {
        this.mScanCallBack = callBack;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="蓝牙服务相关">

    /**
     * 初始化蓝牙的广播对象
     *
     * @return
     */
    protected BleReceiver initBleReceiver() {
        return new BleReceiver() {

            protected void onGattRssiResult(String address, int rssi) {
                super.onGattRssiResult(address, rssi);
            }

            protected void onGattCommandResult(String address, byte[] datas) {
                super.onGattCommandResult(address, datas);
                onGattCommandParsing(address, datas);
            }

            protected void onGattStateChange(String address, int state) {
                super.onGattStateChange(address, state);
            }

            protected void onBluetoothStateChanged(int state) {
                super.onBluetoothStateChanged(state);
            }
        };
    }

    protected abstract void onGattCommandParsing(String address, byte[] datas);

    public void bindService() {
        SnbLog.si(Constant.LOG_TAG, getLogStr("去绑定服务"));
        Context context = mActivityWeakReference.get();
        if(context != null){
            Intent intent = new Intent(context, BluetoothService.class);
            if (lastConnectAddress == null) {
                context.startService(intent);
            }
            context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }else{
            activityNull();
        }
    }

    public void unBindService() {
        SnbLog.si(Constant.LOG_TAG, getLogStr("去解绑服务"));
        if (mBinder != null && mActivityWeakReference.get() != null) {
            mActivityWeakReference.get().unbindService(mServiceConnection);
        }
    }

    private String lastConnectAddress;

    /**
     * 连接设备
     *
     * @param address 设备地址
     */
    public void connectionDevice(String address) {
        SnbLog.si(Constant.LOG_TAG,getLogStr("准备连接设备:"+address));
        if (mBinder != null) {
            if (address != null && !address.equals(lastConnectAddress)) {
                if (lastConnectAddress != null) {
                    SnbLog.si(Constant.LOG_TAG,getLogStr("断开老的连接："+lastConnectAddress));
                    mBinder.unConnectDevice(lastConnectAddress);
                }
                SnbLog.si(Constant.LOG_TAG,getLogStr("连接新的地址："+address));
                mBinder.connectDevice(mActivityWeakReference.get(), address, getDeviceRuler());
                lastConnectAddress = address;
            } else {
                SnbLog.si(Constant.LOG_TAG,getLogStr("地址为空或老地址和新地址一样"));
            }
        } else {
            SnbLog.si(Constant.LOG_TAG,getLogStr("服务不存在"));
        }
    }

    /**
     * 断开设备连接
     */
    public void disConnectionDevice() {
        if (mBinder != null && lastConnectAddress != null) {
            mBinder.unConnectDevice(lastConnectAddress);
            lastConnectAddress = "";
        }
    }

    /**
     * 发送指令
     *
     * @param data 指令数据
     */
    public void sendCommand(byte[] data) {
        if (mBinder != null && lastConnectAddress != null) {
            mBinder.sendCommand(lastConnectAddress, data);
        }
    }


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="其他生命周期操作">

    /**
     * 创建初始化对象，只需要执行一次就可以了
     */
    public void onCreate() {
        SnbLog.si(Constant.LOG_TAG, getLogStr("调用创建"));
        mBluetoothManager.registerOnBLEScanCallBack(mOnBLEScanCallBack);
        if (mActivityWeakReference.get() != null) {
            SnbLog.si(Constant.LOG_TAG, getLogStr("注册监听"));
            Context context = mActivityWeakReference.get();
            context.registerReceiver(mBleReceiver, BleReceiver.makeIntentFilter());
            bindService();
        } else {
            activityNull();
        }
    }

    /**
     * 当不用的时候需要调用该方法进行销毁
     */
    public void onDestory() {
        SnbLog.si(Constant.LOG_TAG, getLogStr("调用销毁"));
        unBindService();
        mBluetoothManager.unregisterOnBLEScanCallBack(mOnBLEScanCallBack);
        mActivityWeakReference.get().unregisterReceiver(mBleReceiver);

    }
    // </editor-fold>

    private void activityNull(){
        throw new IllegalArgumentException("activity 为null 请确保 创建的activity的生命周期大于该类的生命周期");
    }

    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }
}
