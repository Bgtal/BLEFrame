package blq.ssnb.bleframe.inf;

import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.IBinder;

import java.util.Map;

import blq.ssnb.bleframe.ConnectBean;
import blq.ssnb.bleframe.service.BleGattCallBack;

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
public interface IService {
    /**
     * 初始化Binder
     *
     * @return
     */
    IBinder initBinder();

    /**
     * 获取 蓝牙 回调
     *
     * @return 回调对象
     */
    BluetoothGattCallback initBleGattCallBack();

    /**
     * 获取蓝牙回调
     * @return 回调对象
     */
    BluetoothGattCallback getBleGattCallBack();

    /**
     * 获取缓存对象
     *
     * @return
     */
    Map<String, ConnectBean> initConnectCacheMap();

    /**
     * 获取缓存对象
     *
     * @return
     */
    Map<String, ConnectBean> getConnectCacheMap();

    /**
     * 获取上下文对象
     * @return
     */
    Context getContext();
}
