package blq.ssnb.bleframe.helper;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.lang.ref.WeakReference;

import blq.ssnb.bleframe.listener.HelperListenerProxy;
import blq.ssnb.bleframe.SnbBluetoothManager;
import blq.ssnb.bleframe.inf.IBleDeviceRuler;
import blq.ssnb.bleframe.inf.IServiceBinder;
import blq.ssnb.bleframe.listener.OnBleError;
import blq.ssnb.bleframe.listener.OnBleScan;
import blq.ssnb.bleframe.listener.OnBluetoothStateChange;
import blq.ssnb.bleframe.listener.OnCommandCallBack;
import blq.ssnb.bleframe.listener.OnGattStateChange;
import blq.ssnb.bleframe.service.BaseBleService;
import blq.ssnb.snbutil.SnbLog;

import static blq.ssnb.bleframe.Constant.LOG_TAG;
import static blq.ssnb.bleframe.Constant.ErrorInfo;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-29
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *  使用蓝牙BLE 必须 申请如下权限
 * 权限
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 *
 * android 6.0 以上需要定位权限才能使用蓝牙 ble 功能(动态申请)
 *
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * ================================================
 * </pre>
 */
public abstract class BaseBleDeviceHelper {


    protected WeakReference<Activity> mActivityWeakReference;

    private SnbBluetoothManager mBluetoothManager;
    private SnbBluetoothManager.OnBLEScanCallBack mOnBLEScanCallBack;

    private HelperListenerProxy mHelperListenerProxy;

    public HelperListenerProxy getHelperListenerProxy() {
        return mHelperListenerProxy;
    }

    // <editor-fold defaultstate="collapsed" desc="和蓝牙服务相关">
    private ServiceConnection mServiceConnection;
    private IServiceBinder mBinder;
    private IBleDeviceRuler mDeviceRuler;
    private BleReceiver mBleReceiver;
    // </editor-fold>

    public BaseBleDeviceHelper(Activity activity) {
        mActivityWeakReference = new WeakReference<>(activity);
        mBluetoothManager = SnbBluetoothManager.singleton();
        initHelperListenerProxy();
        initBLEScanCallBack();
        initServiceConnection();
        mBleReceiver = initBleReceiver();

    }
    // <editor-fold defaultstate="collapsed" desc="初始化一些监听">

    private void initServiceConnection() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof IServiceBinder) {
                    mBinder = (IServiceBinder) service;
                    SnbLog.se(LOG_TAG, getLogStr("服务连接：" + name));
                } else {
                    SnbLog.se(LOG_TAG, getLogStr("服务连接-但是不是IServiceBinder对象" + name));
                }
                onBleServiceConnected(name, service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                SnbLog.se(LOG_TAG, getLogStr("服务断开" + name));
                //断开服务的时候需要将连接断开并且置空
                if (mBinder != null) {
                    disConnectionDevice();
                    mBinder = null;
                }
                onBleServiceDisconnected(name);
            }
        };
    }

    private void initHelperListenerProxy() {
        mHelperListenerProxy = new HelperListenerProxy() {
            @Override
            public void onConnected(String address) {
                super.onConnected(address);
                //如果连接成功 那么当前的地址替换为 该地址
                mCurrentAddress = address;
            }

            @Override
            public void onDisconnected(String address) {
                super.onDisconnected(address);
                //断开后需要清空当前地址和临时地址
                mCurrentAddress = null;
                mTempAddress = null;
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                super.onError(errorCode, errorMsg);
                if (errorCode == ErrorInfo.BLE_GATT_STATE_CHANGE_FAIL.getCode()) {
                    //蓝牙状态改变失败 1.连接失败 2.断开失败
                    //无论哪种失败，我都清空这个连接
                    mCurrentAddress = null;
                    mTempAddress = null;
                }
            }

            @Override
            public void onCommandResult(byte[] data) {
                super.onCommandResult(data);
                onGattCommandParsing(data);
            }
        };
    }

    private void initBLEScanCallBack() {
        mOnBLEScanCallBack = new SnbBluetoothManager.OnBLEScanCallBack() {
            @Override
            public void onScanResult(ScanResult result) {
                if (result != null && getDeviceRuler() != null) {
                    if (getDeviceRuler().isDevice(result)) {
                        BluetoothDevice device = result.getDevice();
                        SnbLog.se(LOG_TAG, getLogStr("扫描到符合的设备:" + device.toString()));
                        if (mHelperListenerProxy != null) {
                            mHelperListenerProxy.onScanResult(device);
                        }
                    }
                }
            }

            @Override
            public void onScanFail(int errorCode) {
                SnbLog.se(LOG_TAG, getLogStr("扫描失败：" + errorCode));
                sendError(ErrorInfo.BLE_SCAN_FAIL);
            }

            @Override
            public void onScanTimeOut() {
                SnbLog.se(LOG_TAG, getLogStr("扫描超时"));
                if (mHelperListenerProxy != null) {
                    mHelperListenerProxy.onScanTimeOut();
                }
            }
        };
    }

    /**
     * 子类如果需要自己实现binder 的需求的话，这里可以拿到
     */
    protected void onBleServiceConnected(ComponentName name, IBinder service) {

    }

    protected void onBleServiceDisconnected(ComponentName name) {

    }

    /**
     * 初始化蓝牙广播对象
     * 也是留给子类扩展广播用的
     */
    protected BleReceiver initBleReceiver() {
        return new BleReceiver(this);
    }

    // </editor-fold>

    /**
     * 蓝牙一些判断和一些开启关闭等操作都由他来完成
     */
    public SnbBluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public void startScan(){
        if(isDeviceSupport()){
            getBluetoothManager().startScanLE();
        }else{
            sendError(ErrorInfo.BLUE_TOOTH_UN_SUPPORT);
        }
    }

    public void stopScan(){
        getBluetoothManager().stopScanLE();
    }

    /**
     * 获得 设备的规则对象
     */
    private IBleDeviceRuler getDeviceRuler() {
        if (mDeviceRuler == null) {
            mDeviceRuler = initDeviceRuler();
        }
        return mDeviceRuler;
    }

    /**
     * 初始化设备的加载规则
     */
    protected abstract IBleDeviceRuler initDeviceRuler();

    private boolean isDeviceSupport() {
        return getBluetoothManager().isSupport() && getBluetoothManager().isSupportLE();
    }

    // <editor-fold defaultstate="collapsed" desc="蓝牙服务相关">

    public final boolean isCurrentConnecting(String address) {
        if (mTempAddress != null) {
            return mTempAddress.equals(address);
        }
        return false;
    }

    /**
     * 解析回调的值
     * @param datas
     */
    protected abstract void onGattCommandParsing(byte[] datas);

    //绑定服务
    protected void bindService() {
        SnbLog.si(LOG_TAG, getLogStr("去绑定服务"));
        Context context = mActivityWeakReference.get();
        if (context != null) {
            Intent intent = new Intent(context, getBleService());
            if (mBinder == null) {
                context.startService(intent);
            }
            context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            activityNull();
        }
    }

    protected Class<? extends Service> getBleService() {
        return BaseBleService.class;
    }

    //解绑服务
    protected void unBindService() {
        SnbLog.si(LOG_TAG, getLogStr("去解绑服务"));
        if (mBinder != null && mActivityWeakReference.get() != null) {
            mActivityWeakReference.get().unbindService(mServiceConnection);
        }
    }

    /**
     * 当前地址改变的地方只有两个
     * 当连接成功的时候
     * 当断开成功的时候
     */
    private String mCurrentAddress;

    /**
     * 临时地址改变的地方
     * 1.连接的时候
     * 2.断开连接的时候
     * 3.连接失败的时候
     */
    private String mTempAddress;

    /**
     * 连接设备
     *
     * @param address 设备地址
     */
    public void connectionDevice(String address) {
        if (!isDeviceSupport()) {
            sendError(ErrorInfo.BLUE_TOOTH_UN_SUPPORT);
            return;
        }
        if (address == null || address.trim().equals("")) {
            sendError(ErrorInfo.BLE_CONNECT_ADDRESS_IS_NULL);
            return;
        }

        SnbLog.si(LOG_TAG, getLogStr("准备连接设备:" + address));

        if (mBinder != null) {
            //连接地址与当前已连接地址不一致
            if (address.equals(mCurrentAddress)) {
                //如果地址一样 重新连一下
                mBinder.connectDevice(mActivityWeakReference.get(), address, getDeviceRuler());
            } else {//地址不一样 ，先设置临时地址
                mTempAddress = address;
                if (mCurrentAddress != null) {//如果老的连接地址不为null 说明老的有连接过，先断开
                    SnbLog.si(LOG_TAG, getLogStr("断开老的连接：" + mCurrentAddress));
                    mBinder.unConnectDevice(mCurrentAddress);
                }
                SnbLog.si(LOG_TAG, getLogStr("连接新的地址：" + address));
                //设置临时连接地址 //因为这个地址可能连接会失败
                boolean isSuccess = mBinder.connectDevice(mActivityWeakReference.get(), address, getDeviceRuler());
                if (isSuccess) {
                    //调用成功不用管,到对应的 onGattStateChange 会有回调
                } else {
                    //发送回调说错误状态改变失败了
                    sendError(ErrorInfo.BLE_GATT_STATE_CHANGE_FAIL);
                }
            }
        } else {
            SnbLog.si(LOG_TAG, getLogStr("服务不存在"));
            sendError(ErrorInfo.SERVICE_UN_BIND_CONNECT_DEVICE);
        }
    }

    /**
     * 断开设备连接
     */
    public void disConnectionDevice() {
        if (mBinder != null) {
            //断开连接
            mBinder.unConnectDevice(mCurrentAddress);
        } else {
            SnbLog.si(LOG_TAG, getLogStr("服务不存在"));
            sendError(ErrorInfo.SERVICE_UN_BIND_DISCONNECT_DEVICE);
        }
    }

    /**
     * 发送指令
     *
     * @param data 指令数据
     */
    public void sendCommand(byte[] data) {
        if (mBinder != null) {
            if (mCurrentAddress != null && !mCurrentAddress.trim().equals("")) {
                mBinder.sendCommand(mCurrentAddress, data);
            } else {
                sendError(ErrorInfo.SERVICE_UN_BIND_COMMAND);
            }
        } else {
            SnbLog.si(LOG_TAG, getLogStr("服务不存在"));
            sendError(ErrorInfo.SERVICE_UN_BIND_COMMAND);
        }
    }

    // </editor-fold>

    /**
     * 创建初始化对象，只需要执行一次就可以了
     */
    public void onCreate() {
        SnbLog.si(LOG_TAG, getLogStr("调用创建"));
        mBluetoothManager.registerOnBLEScanCallBack(mOnBLEScanCallBack);
        if (mActivityWeakReference.get() != null) {
            SnbLog.si(LOG_TAG, getLogStr("注册监听"));
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
    public void onDestroy() {
        SnbLog.si(LOG_TAG, getLogStr("调用销毁"));
        unBindService();
        mBluetoothManager.unregisterOnBLEScanCallBack(mOnBLEScanCallBack);
        mActivityWeakReference.get().unregisterReceiver(mBleReceiver);

    }

    // <editor-fold defaultstate="collapsed" desc="回调对象">

    /**
     * 设置扫描的回调
     */
    public void setOnBleScan(OnBleScan callBack) {
        if (mHelperListenerProxy != null) {
            mHelperListenerProxy.setOnBleScan(callBack);
        }
    }

    /**
     * 设置蓝牙状态回到
     */
    public void setOnBluetoothStateChange(OnBluetoothStateChange callback) {
        if (mHelperListenerProxy != null) {
            mHelperListenerProxy.setOnBluetoothStateChange(callback);
        }
    }

    /**
     * 设置设备连接状态回调
     */
    public void setOnGattStateChange(OnGattStateChange onGattStateChange) {
        if (mHelperListenerProxy != null) {
            mHelperListenerProxy.setOnGattStateChange(onGattStateChange);
        }
    }

    public void setOnBleError(OnBleError bleError) {
        if (mHelperListenerProxy != null) {
            mHelperListenerProxy.setOnBleError(bleError);
        }
    }

    public void setOnCommandCallBack(OnCommandCallBack commandCallBack) {
        if (mHelperListenerProxy != null) {
            mHelperListenerProxy.setOnCommandCallBack(commandCallBack);
        }
    }

    // </editor-fold>

    protected final void activityNull() {
        throw new IllegalArgumentException("activity 为null 请确保 创建的activity的生命周期大于该类的生命周期");
    }

    protected final void sendError(ErrorInfo info) {
        if (mHelperListenerProxy != null) {
            mHelperListenerProxy.onError(info.getCode(), info.getMsg());
        }
    }

    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }
}
