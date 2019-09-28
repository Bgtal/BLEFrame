package blq.ssnb.bleframe;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      所有的广播类型都在这里写
 *      方便快速寻找
 * ================================================
 * </pre>
 */
public class SendBroadcastUtil {

    public static final String KEY_STR_ADDRESS = "bleAddress";
    public static final String KEY_BOL_SUCCESS = "isSuccess";


    /**
     * GATT 状态改变回调
     * key:
     * <p/>
     * {@link SendBroadcastUtil#KEY_STR_ADDRESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_BOL_SUCCESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_GATT_STATE_CHANGE_INT_STATE}
     *
     */
    public static final String ACTION_GATT_STATE_CHANGE = "com.apiyoo.ssnb.action.ACTION_GATT_STATE_CHANGE";
    public static final String KEY_GATT_STATE_CHANGE_INT_STATE = "gatt_state_change_int_state";
    public static final int VALUE_GATT_STATE_CHANGE_STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    public static final int VALUE_GATT_STATE_CHANGE_STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    public static final int VALUE_GATT_STATE_CHANGE_STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING;
    public static final int VALUE_GATT_STATE_CHANGE_STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;

    public static void sendGattStateChange(Context context, boolean isSuccess, String address, int status) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_STR_ADDRESS, address);
        bundle.putBoolean(KEY_BOL_SUCCESS, isSuccess);
        bundle.putInt(KEY_GATT_STATE_CHANGE_INT_STATE, status);
        sendBleBroadcast(context, ACTION_GATT_STATE_CHANGE, bundle);
    }


    /**
     * 服务被发现后的执行结果结果，
     * 如果失败意味着 与设备的连接没有真正的建立起来
     * key:
     * <p/>
     * {@link SendBroadcastUtil#KEY_STR_ADDRESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_BOL_SUCCESS}
     */
    public static final String ACTION_GATT_DISCOVERED = "com.apiyoo.ssnb.action.ACTION_GATT_DISCOVERED";

    public static void sendServicesDiscovered(Context context, String address, boolean isSuccess) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_STR_ADDRESS, address);
        bundle.putBoolean(KEY_BOL_SUCCESS, isSuccess);
        sendBleBroadcast(context, ACTION_GATT_DISCOVERED, bundle);
    }


    /**
     * 设备通信之间的回调
     *
     * key:
     * <p/>
     * {@link SendBroadcastUtil#KEY_STR_ADDRESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_BOL_SUCCESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_GATT_COMMAND_RESULT_BYTE_ARRAY_DATA}
     *
     */
    public static final String ACTION_GATT_COMMAND_RESULT = "com.apiyoo.ssnb.action.ACTION_GATT_COMMAND_RESULT";
    public static final String KEY_GATT_COMMAND_RESULT_BYTE_ARRAY_DATA = "gatt_command_result_byte_array_data";

    public static void sendGattCommandResult(Context context, boolean isSuccess, String address, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_STR_ADDRESS, address);
        bundle.putBoolean(KEY_BOL_SUCCESS, isSuccess);
        bundle.putByteArray(KEY_GATT_COMMAND_RESULT_BYTE_ARRAY_DATA, data);
        sendBleBroadcast(context, ACTION_GATT_COMMAND_RESULT, bundle);
    }

    /**
     * 读取芯片信号强度
     *
     * key:
     * <p/>
     * {@link SendBroadcastUtil#KEY_STR_ADDRESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_BOL_SUCCESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_READ_REMOTE_RSSI_INT_RSSI}
     *
     */
    public static final String ACTION_READ_REMOTE_RSSI = "com.apiyoo.ssnb.action.ACTION_READ_REMOTE_RSSI";
    public static final String KEY_READ_REMOTE_RSSI_INT_RSSI = "read_remote_rssi_int_rssi";


    public static void sendRemoteRssi(Context context,boolean isSuccess,String address,int rssi){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_STR_ADDRESS,address);
        bundle.putBoolean(KEY_BOL_SUCCESS, isSuccess);
        bundle.putInt(KEY_READ_REMOTE_RSSI_INT_RSSI, rssi);
        sendBleBroadcast(context,ACTION_READ_REMOTE_RSSI, bundle);
    }

    /**
     * 获取新的包数据大小
     * {@link SendBroadcastUtil#KEY_STR_ADDRESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_BOL_SUCCESS}
     * <p/>
     * {@link SendBroadcastUtil#KEY_MTU_CHANGE_INT_MTU}
     *
     */
    public static final String ACTION_MTU_CHANGE = "com.apiyoo.ssnb.action.ACTION_MTU_CHANGE";
    public static final String KEY_MTU_CHANGE_INT_MTU = "mtu_change_int_mtu";

    public static void sendMTUChange(Context context,boolean isSuccess,String address,int mtu){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_STR_ADDRESS,address);
        bundle.putBoolean(KEY_BOL_SUCCESS, isSuccess);
        bundle.putInt(KEY_MTU_CHANGE_INT_MTU, mtu);
        sendBleBroadcast(context,ACTION_MTU_CHANGE, bundle);
    }


    public static void sendBleBroadcast(Context context, String action, Bundle bundle) {
        Intent intent = new Intent(action);
        if (bundle == null) {
            bundle = new Bundle();
        }
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
    }
}
