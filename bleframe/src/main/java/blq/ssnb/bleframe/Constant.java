package blq.ssnb.bleframe;

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
        return SnbLog.getBuilder(LOG_TAG).setTag(LOG_TAG);
    }

    public static SnbLog.Builder isLogOpen(boolean isOpen) {
        return getBleLogBuilder().isOpen(isOpen);
    }

    public enum ErrorInfo {
        /**
         * {@link blq.ssnb.bleframe.listener.OnBleScan}
         * 扫描错误的信息
         */
        // <editor-fold defaultstate="collapsed" desc="OnBleScan 1000">
        BLE_SCAN_FAIL(1001, "扫描设备失败"),
        // </editor-fold>

        /**
         * {@link blq.ssnb.bleframe.listener.OnBluetoothStateChange}
         * 蓝牙状态的错误信息返回
         */
        // <editor-fold defaultstate="collapsed" desc="OnBluetoothStateChange 2000">
        BLUETOOTH_STATE_UN_KNOW(2001, "蓝牙状态未知"),
        // </editor-fold>

        /**
         * {@link blq.ssnb.bleframe.listener.OnGattStateChange}
         * 蓝牙BLE状态的错误情况
         */
        // <editor-fold defaultstate="collapsed" desc="OnGattStateChange 3000">
        BLE_GATT_STATE_UN_KNOW(3001, "蓝牙BLE协议状态改变未知"),

        BLE_GATT_STATE_CHANGE_FAIL(3002, "蓝牙BLE协议状态改变失败"),

        BLE_CONNECT_ADDRESS_IS_NULL(3003, "蓝牙BLE 连接地址为空"),

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="OnCommandCallBack 4000">
        COMMAND_FAIL(4001, "GATT 通信失败"),
        READ_RSSI_FAIL(4001, "读取信号强度失败"),
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="OnBleError 5000">
        /**
         * {@link blq.ssnb.bleframe.listener.OnBleError}
         * <p>
         * 其他的错误信息
         */
        BLUE_TOOTH_UN_SUPPORT(5000, "设备不支持蓝牙BLE"),
        SERVICE_UN_BIND_CONNECT_DEVICE(5001, "服务未启动"),
        SERVICE_UN_BIND_DISCONNECT_DEVICE(5002, "服务未启动"),
        SERVICE_UN_BIND_COMMAND(5002, "服务未启动"),

        // </editor-fold>
        ;

        private int code;
        private String msg;

        ErrorInfo(int errorCode, String errorMsg) {
            this.code = errorCode;
            this.msg = errorMsg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
