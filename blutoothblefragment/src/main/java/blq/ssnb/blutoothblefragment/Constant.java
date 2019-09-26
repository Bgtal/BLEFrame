package blq.ssnb.blutoothblefragment;

import android.bluetooth.BluetoothAdapter;

import blq.ssnb.snbutil.SnbLog;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      常量类，
 *      存储了广播的action 以及传出的key
 *      错误code值
 *      SnbLog中使用的Tag等
 * ================================================
 * </pre>
 */
public class Constant {

    public static final String LOG_TAG = "蓝牙BLE";

    public static SnbLog.Builder getBleLogBuilder() {
        return SnbLog.getBuilder(LOG_TAG);
    }

    public static void isLogOpen(boolean isOpen) {
        SnbLog.getBuilder(LOG_TAG).isOpen(isOpen);
    }

    public interface ErrorInfo {
        int BLE_SCAN_FAIL_CODE = 1000;
        String BLE_SCAN_FAIL_MSG = "扫描设备失败";
    }

    public interface BroadcastActionAndKey {

        /**
         * 设备地址
         * value : String 地址
         */
        String GLOBAL_KEY_STR_ADDRESS = "bleAddress";
        /**
         * 蓝牙状态改变
         * key :
         * {@link BluetoothAdapter#EXTRA_STATE}
         * value :
         * {@link BluetoothAdapter#STATE_TURNING_ON}
         * {@link BluetoothAdapter#STATE_ON}
         * {@link BluetoothAdapter#STATE_TURNING_OFF}
         * {@link BluetoothAdapter#STATE_OFF}
         */
        String ACTION_BLUETOOTH_CHANGED = BluetoothAdapter.ACTION_STATE_CHANGED;
        /**
         * GATT 连接状态改变
         * key :
         * {@link BroadcastActionAndKey#GLOBAL_KEY_STR_ADDRESS}
         * value : 看对应的注释
         * key :
         * {@link BroadcastActionAndKey#GATT_CONNECTION_STATE_CHANGE_KEY_INT_STATUS}
         * value : 看对应的注释
         */
        String ACTION_GATT_CONNECTION_STATE_CHANGE = "com.apiyoo.ssnb.action.ACTION_GATT_CONNECTION_STATE_CHANGE";
        /**
         * value :
         * {@link android.bluetooth.BluetoothProfile#STATE_CONNECTING}
         * {@link android.bluetooth.BluetoothProfile#STATE_CONNECTED}
         * {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTING}
         * {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTED}
         */
        String GATT_CONNECTION_STATE_CHANGE_KEY_INT_STATUS = "connectionState";

        /**
         * 命令行回调广播
         * key :
         * {@link BroadcastActionAndKey#GLOBAL_KEY_STR_ADDRESS}
         * value :看对应的注释
         * key :
         * {@link BroadcastActionAndKey#GATT_COMMAND_RESULT_BYTE_ARRAY_DATA}
         * value : 看对应的注释
         */
        String ACTION_GATT_COMMAND_RESULT = "com.apiyoo.ssnb.action.ACTION_GATT_COMMAND_RESULT";

        /**
         * 蓝牙设备返回的数据
         * value : byte[]数组
         */
        String GATT_COMMAND_RESULT_BYTE_ARRAY_DATA = "byteArrayData";

        /**
         * 获取强度返回
         * key :
         * {@link BroadcastActionAndKey#GLOBAL_KEY_STR_ADDRESS}
         * value : 看对应的注释
         * key :
         * {@link BroadcastActionAndKey#GATT_COMMAND_RESULT_BYTE_ARRAY_DATA}
         * value : 看对应的注释
         */
        String ACTION_GATT_RSSI_RESULT = "com.apiyoo.ssnb.action.ACTION_GATT_RSSI_RESULT";
        /**
         * 信号强度
         * value : int 类型
         */
        String GATT_RSSI_RESULT_INT_RSSI = "Rssi";

        /**
         * 错误返回
         * key :
         * {@link BroadcastActionAndKey#GLOBAL_KEY_STR_ADDRESS}
         * value : 看对应的注释
         * key :
         * {@link BroadcastActionAndKey#ERROR_INT_ERROR_CODE}
         * value: 看对应注释
         * key :
         * {@link BroadcastActionAndKey#ERROR_STR_ERROR_MSG}
         * value: 看对应注释
         */
        String ACTION_ERROR = "com.apiyoo.ssnb.action.ACTION_GATT_RSSI_RESULT";

        /**
         * 错误码
         * value ：{@link ErrorInfo} 中的XXX_CODE
         */
        String ERROR_INT_ERROR_CODE = "errorCode";

        /**
         * 错误描述
         * value:{@link ErrorInfo} 中XXX_MSG
         */
        String ERROR_STR_ERROR_MSG = "errorMsg";


    }

}
