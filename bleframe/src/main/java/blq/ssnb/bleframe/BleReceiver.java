package blq.ssnb.bleframe;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import blq.ssnb.snbutil.SnbLog;

import static blq.ssnb.bleframe.Constant.LOG_TAG;

import blq.ssnb.bleframe.Constant.*;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      蓝牙BLE的广播类
 *
 *      该类的方法不建议重写，即时重写也要实现 super 方法
 *      否者将会导致功能无法连接起来
 * ================================================
 * </pre>
 */
public class BleReceiver extends BroadcastReceiver {

    protected AbsBleDeviceHelper mDeviceHelper;
    protected HelperListenerProxy mProxy;

    public BleReceiver(AbsBleDeviceHelper deviceHelper) {
        if (deviceHelper == null) {
            throw new IllegalArgumentException("广播需要传入Helper");
        }
        mDeviceHelper = deviceHelper;
        mProxy = mDeviceHelper.getHelperListenerProxy();
    }

    /**
     * BLE 需要送到的广播action
     */
    public static IntentFilter makeIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(SendBroadcastUtil.ACTION_GATT_STATE_CHANGE);
        intentFilter.addAction(SendBroadcastUtil.ACTION_GATT_COMMAND_RESULT);
        intentFilter.addAction(SendBroadcastUtil.ACTION_READ_REMOTE_RSSI);
        intentFilter.addAction(SendBroadcastUtil.ACTION_GATT_DISCOVERED);
        intentFilter.addAction(SendBroadcastUtil.ACTION_MTU_CHANGE);
        return intentFilter;
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            onBluetoothStateChanged(state);
        } else if (SendBroadcastUtil.ACTION_GATT_COMMAND_RESULT.equals(action)) {
            boolean isSuccess = getIsSuccess(intent);
            String address = getAddress(intent);

            byte[] datas = intent.getByteArrayExtra(SendBroadcastUtil.KEY_GATT_COMMAND_RESULT_BYTE_ARRAY_DATA);
            onGattCommandResult(isSuccess, address, datas);
        } else if (SendBroadcastUtil.ACTION_GATT_STATE_CHANGE.equals(action)) {
            boolean isSuccess = getIsSuccess(intent);
            String address = getAddress(intent);

            int state = intent.getIntExtra(SendBroadcastUtil.KEY_GATT_STATE_CHANGE_INT_STATE, -1);
            onGattStateChange(isSuccess, address, state);
        } else if (SendBroadcastUtil.ACTION_READ_REMOTE_RSSI.equals(action)) {
            boolean isSuccess = getIsSuccess(intent);
            String address = getAddress(intent);

            int rssi = intent.getIntExtra(SendBroadcastUtil.KEY_READ_REMOTE_RSSI_INT_RSSI, -1);
            onGattRssiResult(isSuccess, address, rssi);
        } else if (SendBroadcastUtil.ACTION_GATT_DISCOVERED.equals(action)) {
            boolean isSuccess = getIsSuccess(intent);
            String address = getAddress(intent);

            onSetDiscoveredResult(address, isSuccess);
        } else if (SendBroadcastUtil.ACTION_MTU_CHANGE.equals(action)) {
            boolean isSuccess = getIsSuccess(intent);
            String address = getAddress(intent);

            int size = intent.getIntExtra(SendBroadcastUtil.KEY_MTU_CHANGE_INT_MTU, -1);
            onSetMTUResult(isSuccess, address, size);
        } else {
            onReceiveOther(context, action, intent);

        }
    }

    protected final String getAddress(Intent intent) {
        return intent.getStringExtra(SendBroadcastUtil.KEY_STR_ADDRESS);
    }

    protected final boolean getIsSuccess(Intent intent) {
        return intent.getBooleanExtra(SendBroadcastUtil.KEY_BOL_SUCCESS, false);
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
        SnbLog.se(LOG_TAG, getLogStr("手机蓝牙状态回调:" + state));
        if (mProxy != null) {
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    mProxy.onTurningOn();
                    break;

                case BluetoothAdapter.STATE_ON:
                    mProxy.onOn();
                    break;

                case BluetoothAdapter.STATE_TURNING_OFF:
                    //关闭的时候需要断开连接
                    mDeviceHelper.disConnectionDevice();
                    mProxy.onTurningOff();
                    break;

                case BluetoothAdapter.STATE_OFF:
                    mProxy.onOff();
                    break;
                default:
                    mDeviceHelper.sendError(ErrorInfo.BLUETOOTH_STATE_UN_KNOW);
            }
        }
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
    protected void onGattStateChange(boolean isSuccess, String address, int state) {
        SnbLog.se(LOG_TAG, getLogStr("设备连接状态回调:" + address + " === " + state));
        if (mProxy != null) {
            if (isSuccess) {//如果操作成功
                if (mDeviceHelper.isCurrentConnecting(address)) {//如果临时地址和当前返回地址一样的话
                    // 说明是我要的回调
                    switch (state) {
                        case SendBroadcastUtil.VALUE_GATT_STATE_CHANGE_STATE_CONNECTING:
                            mProxy.onConnecting(address);
                            break;
                        case SendBroadcastUtil.VALUE_GATT_STATE_CHANGE_STATE_CONNECTED:
                            mProxy.onConnected(address);
                            break;
                        case SendBroadcastUtil.VALUE_GATT_STATE_CHANGE_STATE_DISCONNECTING:
                            mProxy.onDisconnecting(address);
                            break;
                        case SendBroadcastUtil.VALUE_GATT_STATE_CHANGE_STATE_DISCONNECTED:
                            mProxy.onDisconnected(address);
                            break;
                        default:
                            mDeviceHelper.sendError(ErrorInfo.BLE_GATT_STATE_UN_KNOW);
                    }
                }
            } else {
                if (mDeviceHelper.isCurrentConnecting(address)) {
                    //如果是临时地址的话，可能连接失败或者断开失败，一般断开失败不用管
                    mDeviceHelper.sendError(ErrorInfo.BLE_GATT_STATE_CHANGE_FAIL);
                }
            }
        }
    }


    /**
     * 设备数据的回调
     *
     * @param address 回调设备的地址
     * @param datas   回调的数据
     */
    protected void onGattCommandResult(boolean isSuccess, String address, byte[] datas) {
        SnbLog.se(LOG_TAG, getLogStr("设备数据回调:" + address + " === " + datas.length));
        if (mDeviceHelper.isCurrentConnecting(address)) {
            //当调用的地址是我当前helper连接地址就执行 解析
            if (isSuccess) {
                mProxy.onCommandResult(datas);
            } else {
                mDeviceHelper.sendError(ErrorInfo.COMMAND_FAIL);
            }
        }
    }


    /**
     * 信号强度的回调
     *
     * @param address 回调的设备地址
     * @param rssi    强度
     */
    protected void onGattRssiResult(boolean isSuccess, String address, int rssi) {
        SnbLog.se(LOG_TAG, getLogStr("信号强度的回调:" + address + " === " + rssi));
        if (mDeviceHelper.isCurrentConnecting(address)) {
            if (isSuccess) {
                if (mProxy != null) {
                    mProxy.onGattRSSIResult(rssi);
                }
            } else {
                mDeviceHelper.sendError(ErrorInfo.READ_RSSI_FAIL);
            }
        }
    }

    /**
     * 设置 MTU 的回调
     */
    protected void onSetMTUResult(boolean isSuccess, String address, int size) {
        SnbLog.se(LOG_TAG, getLogStr("设置MTU的回调:" + address + " === " + size));
        if (mDeviceHelper.isCurrentConnecting(address)) {
            if (isSuccess) {
                if (mProxy != null) {
                    mProxy.onSetMTUResult(size);
                }
            } else {
                mDeviceHelper.sendError(ErrorInfo.READ_RSSI_FAIL);
            }
        }
    }

    /**
     * 设备扫描回调
     *
     * @param address   地址
     * @param isSuccess 设置被发现是否成功
     */
    protected void onSetDiscoveredResult(String address, boolean isSuccess) {

    }

    /**
     * 除了上述的几种情况外 其他的回到继承后可以在这里操作
     */
    protected void onReceiveOther(Context context, String action, Intent intent) {
        SnbLog.se(LOG_TAG, getLogStr("监听到其他回调:" + action));
    }


    private String getLogStr(String msg) {
        return "蓝牙-广播:" + msg;
    }

}
