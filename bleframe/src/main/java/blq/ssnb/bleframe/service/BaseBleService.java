package blq.ssnb.bleframe.service;

import android.app.Service;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import blq.ssnb.bleframe.ConnectBean;
import blq.ssnb.bleframe.inf.IService;
import blq.ssnb.snbutil.SnbLog;

import static blq.ssnb.bleframe.Constant.LOG_TAG;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-29
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class BaseBleService extends Service implements IService {

    private IBinder mIBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        SnbLog.se(LOG_TAG, getLogStr("服务绑定"));
        if (mIBinder == null) {
            mIBinder = initBinder();
        }
        return mIBinder;
    }

    @Override
    public IBinder initBinder() {
        return new BleBinder(this);
    }

    private BluetoothGattCallback mGattCallback;
    @Override
    public BluetoothGattCallback initBleGattCallBack() {
        return new BleGattCallBack(this);
    }

    @Override
    public BluetoothGattCallback getBleGattCallBack() {
        if(mGattCallback == null){
            mGattCallback = initBleGattCallBack();
        }
        return mGattCallback;
    }

    /**
     *
     * 移除对象只有在两个地方进行
     * 1.连接断开的时候
     * 2.执行连接或断开连接，但是失败的时候
     * 增加对象只有一个地方进行
     * 1.连接设备地址的时候
     */
    private Map<String, ConnectBean> mConnectBeanMap = new HashMap<>();

    @Override
    public Map<String, ConnectBean> initConnectCacheMap() {
        return new HashMap<>();
    }

    @Override
    public Map<String, ConnectBean> getConnectCacheMap() {
        if (mConnectBeanMap == null) {
            mConnectBeanMap = initConnectCacheMap();
        }
        return mConnectBeanMap;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SnbLog.se(LOG_TAG, getLogStr("服务解绑"));
        return super.onUnbind(intent);
    }

    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }
}
