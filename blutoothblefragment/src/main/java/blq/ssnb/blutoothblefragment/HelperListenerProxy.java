package blq.ssnb.blutoothblefragment;

import android.bluetooth.BluetoothDevice;

import blq.ssnb.blutoothblefragment.listener.OnCommandCallBack;
import blq.ssnb.blutoothblefragment.listener.OnBleError;
import blq.ssnb.blutoothblefragment.listener.OnBleScan;
import blq.ssnb.blutoothblefragment.listener.OnBluetoothStateChange;
import blq.ssnb.blutoothblefragment.listener.OnGattStateChange;
import blq.ssnb.blutoothblefragment.Constant.ErrorInfo;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-28
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class HelperListenerProxy implements OnBleScan, OnBluetoothStateChange, OnGattStateChange, OnCommandCallBack {

    private OnBleScan mOnBleScan;

    public void setOnBleScan(OnBleScan onBleScan) {
        mOnBleScan = onBleScan;
    }

    @Override
    public void onScanResult(BluetoothDevice device) {
        if (mOnBleScan != null) {
            mOnBleScan.onScanResult(device);
        }
    }

    @Override
    public void onScanTimeOut() {
        if (mOnBleScan != null) {
            mOnBleScan.onScanTimeOut();
        }
    }

    private OnBluetoothStateChange mOnBluetoothStateChange;

    public void setOnBluetoothStateChange(OnBluetoothStateChange onBluetoothStateChange) {
        mOnBluetoothStateChange = onBluetoothStateChange;
    }

    @Override
    public void onTurningOn() {
        if (mOnBluetoothStateChange != null) {
            mOnBluetoothStateChange.onTurningOn();
        }
    }

    @Override
    public void onOn() {
        if (mOnBluetoothStateChange != null) {
            mOnBluetoothStateChange.onOn();
        }
    }

    @Override
    public void onTurningOff() {
        if (mOnBluetoothStateChange != null) {
            mOnBluetoothStateChange.onTurningOff();
        }
    }

    @Override
    public void onOff() {
        if (mOnBluetoothStateChange != null) {
            mOnBluetoothStateChange.onOff();
        }
    }

    private OnGattStateChange mOnGattStateChange;

    public void setOnGattStateChange(OnGattStateChange onGattStateChange) {
        mOnGattStateChange = onGattStateChange;
    }

    @Override
    public void onConnecting(String address) {
        if (mOnGattStateChange != null) {
            mOnGattStateChange.onConnecting(address);
        }
    }

    @Override
    public void onConnected(String address) {
        if (mOnGattStateChange != null) {
            mOnGattStateChange.onConnected(address);
        }
    }

    @Override
    public void onDisconnecting(String address) {
        if (mOnGattStateChange != null) {
            mOnGattStateChange.onDisconnecting(address);
        }
    }

    @Override
    public void onDisconnected(String address) {
        if (mOnGattStateChange != null) {
            mOnGattStateChange.onDisconnected(address);
        }
    }

    private OnBleError mOnBleError;

    public void setOnBleError(OnBleError onBleError) {
        mOnBleError = onBleError;
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        OnBleError error;
        if (errorCode == ErrorInfo.BLE_SCAN_FAIL.getCode()) {
            error = mOnBleScan;
        } else if (errorCode == ErrorInfo.BLUETOOTH_STATE_UN_KNOW.getCode()) {
            error = mOnBluetoothStateChange;
        } else if (errorCode == ErrorInfo.BLE_GATT_STATE_UN_KNOW.getCode()
                || errorCode == ErrorInfo.BLE_GATT_STATE_CHANGE_FAIL.getCode()) {
            error = mOnGattStateChange;
        } else {
            error = mOnBleError;
        }

        if (error != null) {
            error.onError(errorCode, errorMsg);
        }
    }

    private OnCommandCallBack mOnCommandCallBack;

    public void setOnCommandCallBack(OnCommandCallBack onCommandCallBack) {
        mOnCommandCallBack = onCommandCallBack;
    }

    @Override
    public void onGattRSSIResult(int rssi) {
        if (mOnCommandCallBack != null) {
            mOnCommandCallBack.onGattRSSIResult(rssi);
        }
    }

    @Override
    public void onSetMTUResult(int newSize) {
        if (mOnCommandCallBack != null) {
            mOnCommandCallBack.onSetMTUResult(newSize);
        }
    }

    @Override
    public void onCommandResult(byte[] data) {
        if(mOnCommandCallBack != null){
            mOnCommandCallBack.onCommandResult(data);
        }
    }
}
