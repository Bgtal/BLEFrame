package blq.ssnb.blutoothblefragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresPermission;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import blq.ssnb.snbutil.SnbCountDownTimer;
import blq.ssnb.snbutil.SnbLog;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-08-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 * 蓝牙的管理器
 * 权限
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 *
 * android 6.0 以上需要定位权限才能使用蓝牙 ble 功能
 *
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * ================================================
 * </pre>
 */
public class SnbBluetoothManager {

    private static SnbBluetoothManager SINGLETON;
    private WeakReference<Application> mWeakContext;
    private BluetoothAdapter mBluetoothAdapter;

    public synchronized static void init(Application application) {
        if (SINGLETON == null) {
            synchronized (SnbBluetoothManager.class) {
                if (SINGLETON == null) {
                    SINGLETON = new SnbBluetoothManager(application);
                }
            }
        }
    }

    private SnbBluetoothManager(Application application) {
        mWeakContext = new WeakReference<>(application);
        getBluetoothAdapter();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        if (mBluetoothAdapter == null) {
            if (mWeakContext == null || mWeakContext.get() == null) {
                throw new IllegalArgumentException("请先在Application中使用SnbBluetoothManager.init()方法");
            }

            BluetoothManager bluetoothManager = (BluetoothManager) mWeakContext.get().getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
        }
        return mBluetoothAdapter;
    }

    public static SnbBluetoothManager singleton() {
        if (SINGLETON == null) {
            throw new IllegalArgumentException("请先在适当的时机调用 init(application) 方法");
        }
        return SINGLETON;
    }

    private boolean isWeakExist() {
        return mWeakContext != null && mWeakContext.get() != null;
    }

    /**
     * 是否支持蓝牙
     *
     * @return true 支持，false 不支持
     */
    public boolean isSupport() {
        return mBluetoothAdapter != null;
    }


    /**
     * 蓝牙是否已经启用
     *
     * @return true 启用了，false 未启用
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public boolean isEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }


    /**
     * 请求打开蓝牙
     *
     * @return true 执行了操作，false 没有执行
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void openBluetooth() {
        if (isSupport() && !isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * 请求打开蓝牙
     *
     * @param context     请求的activity
     * @param requestCode 请求的code code 必须大于0
     *                    请在请求的activity  onActivityResult() 中接收
     *                    成功 返回 {@link Activity#RESULT_OK}
     *                    失败 返回 {@link Activity#RESULT_CANCELED}
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void openBluetoothForResult(Activity context, int requestCode) {
        //如果是蓝牙支持且没有开启，那么就开启蓝牙
        if (isSupport() && !isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, requestCode);
        }
//        return false;
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void closeBluetooth() {
        if (isSupport() && isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void closeBluetoothForResult(Activity context, int requestCode) {
        //如果蓝牙支持且已经打开了就关闭
        if (isSupport() && isEnabled()) {
            Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
            context.startActivityForResult(enableBtIntent, requestCode);
        }
    }

    /**
     * 获取本机的蓝牙名称
     *
     * @return 蓝牙名称，如果设备不支持，无法获得
     */
    public String getName() {
        if (isSupport()) {
            return mBluetoothAdapter.getName();
        }
        return "";
    }

    /**
     * 获取本机的蓝牙地址
     *
     * @return 蓝牙地址，如果设备不支持，无法获得
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public String getAddress() {
        if (isSupport()) {
            return mBluetoothAdapter.getAddress();
        }
        return "";
    }

    /**
     * 是否在扫描
     *
     * @return true 正在扫描
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public boolean isDiscovering() {
        if (isSupport() && isEnabled()) {
            return mBluetoothAdapter.isDiscovering();
        }
        return false;
    }

    /**
     * 启动发现
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void startDiscover() {
        if (isSupport() && isEnabled()) {
            mBluetoothAdapter.startDiscovery();
        }
    }


    /**
     * 取消发现
     * 当找到需要连接的设备后，一定要关闭discover 否者对连接会有影响
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void cancelDiscovery() {
        if (isSupport() && isEnabled() && isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * 启动蓝牙设备被发现功能
     *
     * @param seconds 被发现时间
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void startBeDiscoverEnable(Activity context, int seconds) {
        if (isSupport() && isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
            context.startActivity(discoverableIntent);
        }

    }

    /**
     * 获得已经绑定的蓝牙设备信息
     * <p>
     * 需要打开蓝牙才能获取到设备信息
     *
     * @return 已绑设备信息
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public Set<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> devices = null;
        if (isSupport()) {
            devices = mBluetoothAdapter.getBondedDevices();
        }
        return devices == null ? new HashSet<BluetoothDevice>() : devices;
    }

    /**
     * 获得所有蓝牙相关的广播监听
     *
     * @return 蓝牙相关的广播意图
     */
    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态改变的广播
        filter.addAction(BluetoothDevice.ACTION_FOUND);//找到设备的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描的广播
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        return filter;
    }

    // <editor-fold defaultstate="collapsed" desc="BLE相关接口">

    /**
     * 是否支持low energy
     *
     * @return true 支持 false
     */


    private boolean isScanning = false;
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            SnbLog.si(Constant.LOG_TAG, getLogStr("蓝牙扫描:单个回调"));
            for (OnBLEScanCallBack callBack : mBLEScanCallBacks) {
                if (callBack != null) {
                    callBack.onScanResult(result);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            SnbLog.si(Constant.LOG_TAG, getLogStr("蓝牙扫描:多个回调"));
            for (ScanResult result : results) {
                for (OnBLEScanCallBack callBack : mBLEScanCallBacks) {
                    if (callBack != null) {
                        callBack.onScanResult(result);
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            SnbLog.si(Constant.LOG_TAG, getLogStr("蓝牙扫描:失败=" + errorCode));
            for (OnBLEScanCallBack callBack : mBLEScanCallBacks) {
                if (callBack != null) {
                    callBack.onScanFail(errorCode);
                }
            }
        }
    };

    private List<OnBLEScanCallBack> mBLEScanCallBacks = new ArrayList<>();
    private SnbCountDownTimer bleScanTimer = null;

    private SnbCountDownTimer createDownTimer(long time) {
        if (bleScanTimer != null) {
            bleScanTimer.stop();
        }
        return new SnbCountDownTimer(time, 1000) {
            @Override
            protected void onTick(long remainingMillisecond) {

            }

            @SuppressLint("MissingPermission")
            @Override
            protected void onFinish() {
                //时间到了调用停止扫描
                SnbLog.si(Constant.LOG_TAG, getLogStr("倒计时结束扫描"));
                for (OnBLEScanCallBack callBack : mBLEScanCallBacks) {
                    if (callBack != null) {
                        callBack.onScanTimeOut();
                    }
                }
                stopScanLE();
            }
        };
    }

    public boolean isSupportLE() {
        if (isWeakExist()) {
            return mWeakContext.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }
        return false;
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void startScanLE() {
        //支持蓝牙，并且可用，并且支持ble 并且 没有扫描经典蓝牙
        long NORMAL_SCAN_TIME = 30000;
        startScanLE(NORMAL_SCAN_TIME);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void startScanLE(long continueTime) {
        //支持蓝牙，并且可用，并且支持ble 并且 没有扫描经典蓝牙
        if (isSupport() && isEnabled() && isSupportLE() && !isDiscovering() && !isScanning) {
            //才可以开启扫描
            bleScanTimer = createDownTimer(continueTime);
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            isScanning = true;
            bleScanTimer.start();
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN})
    public void stopScanLE() {
        if (isSupport() && isEnabled() && isSupportLE() && isScanning) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        }
        if (bleScanTimer != null) {
            bleScanTimer.stop();
        }
        bleScanTimer = null;
        isScanning = false;
    }

    /**
     * 注册监听
     * 注意不使用的时候一定要移除监听否者可能导致内存泄漏
     *
     * @param callBack 监听对象
     */
    public void registerOnBLEScanCallBack(OnBLEScanCallBack callBack) {
        mBLEScanCallBacks.add(callBack);
    }

    /**
     * 移除已注册的监听
     *
     * @param callBack
     */
    public void unregisterOnBLEScanCallBack(OnBLEScanCallBack callBack) {
        mBLEScanCallBacks.remove(callBack);
    }


    public interface OnBLEScanCallBack {
        void onScanResult(ScanResult result);

        void onScanFail(int errorCode);

        void onScanTimeOut();
    }
    // </editor-fold>

    private String getLogStr(String msg) {
        return "蓝牙Manager:" + msg;
    }
}

