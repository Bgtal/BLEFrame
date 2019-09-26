package blq.ssnb.blutoothblefragment;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import blq.ssnb.snbutil.SnbLog;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class BleReceiver extends BroadcastReceiver {
    private String logTag = ">>>蓝牙BLE-广播";

    /**
     * BLE 需要送到的广播action
     */
    public static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BroadcastActionAndKey.ACTION_BLUETOOTH_CHANGED);
        intentFilter.addAction(Constant.BroadcastActionAndKey.ACTION_GATT_CONNECTION_STATE_CHANGE);
        intentFilter.addAction(Constant.BroadcastActionAndKey.ACTION_GATT_COMMAND_RESULT);
        intentFilter.addAction(Constant.BroadcastActionAndKey.ACTION_GATT_RSSI_RESULT);
        return intentFilter;
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constant.BroadcastActionAndKey.ACTION_BLUETOOTH_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            onBluetoothStateChanged(state);
        } else if (Constant.BroadcastActionAndKey.ACTION_GATT_COMMAND_RESULT.equals(action)) {
            String address = intent.getStringExtra(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS);
            byte[] datas = intent.getByteArrayExtra(Constant.BroadcastActionAndKey.GATT_COMMAND_RESULT_BYTE_ARRAY_DATA);
            onGattCommandResult(address, datas);
        } else if (Constant.BroadcastActionAndKey.ACTION_GATT_CONNECTION_STATE_CHANGE.equals(action)) {
            String address = intent.getStringExtra(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS);
            int state = intent.getIntExtra(Constant.BroadcastActionAndKey.GATT_CONNECTION_STATE_CHANGE_KEY_INT_STATUS, -1);
            onGattStateChange(address, state);
        } else if (Constant.BroadcastActionAndKey.ACTION_GATT_RSSI_RESULT.equals(action)) {
            String address = intent.getStringExtra(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS);
            int rssi = intent.getIntExtra(Constant.BroadcastActionAndKey.GATT_RSSI_RESULT_INT_RSSI, -1);
            onGattRssiResult(address, rssi);
        } else {
            onReceiveOther(context, intent);
        }
    }

    /**
     * 除了上述的几种情况外 其他的回到继承后可以在这里操作
     *
     * @param context
     * @param intent
     */
    public void onReceiveOther(Context context, Intent intent) {

    }

    /**
     * 信号强度的回调
     *
     * @param address 回调的设备地址
     * @param rssi    强度
     */
    protected void onGattRssiResult(String address, int rssi) {
        SnbLog.e(logTag, "信号强度的回调:" + address + " === " + rssi);
    }

    /**
     * 设备数据的回调
     *
     * @param address 回调设备的地址
     * @param datas   回调的数据
     */
    protected void onGattCommandResult(String address, byte[] datas) {
        SnbLog.e(logTag, "设备数据回调:" + address + " === " + datas.length);
    }

    /**
     * ble gatt状态改变
     *
     * @param address 那个地址的连接状态改变了
     * @param state   状态值
     *                {@link android.bluetooth.BluetoothProfile#STATE_CONNECTING}
     *                {@link android.bluetooth.BluetoothProfile#STATE_CONNECTED}
     *                {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTING}
     *                {@link android.bluetooth.BluetoothProfile#STATE_DISCONNECTED}
     */
    protected void onGattStateChange(String address, int state) {
        SnbLog.e(logTag, "设备连接状态回调:" + address + " === " + state);
    }

    /**
     * 蓝牙状态改变
     *
     * @param state 状态值
     *              {@link BluetoothAdapter#STATE_TURNING_ON}
     *              {@link BluetoothAdapter#STATE_ON}
     *              {@link BluetoothAdapter#STATE_TURNING_OFF}
     *              {@link BluetoothAdapter#STATE_OFF}
     */
    protected void onBluetoothStateChanged(int state) {
        SnbLog.e(logTag, "手机蓝牙状态回调:" + state);
    }


}
