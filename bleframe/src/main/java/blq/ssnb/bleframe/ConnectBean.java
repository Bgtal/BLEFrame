package blq.ssnb.bleframe;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import blq.ssnb.bleframe.inf.IBleDeviceRuler;
import blq.ssnb.snbutil.SnbLog;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      具体的蓝牙BLE连接对象
 *      里面存储了蓝牙连接协议GATT 、通信对象
 *      以及蓝牙设备的一些规则
 * ================================================
 * </pre>
 */
public class ConnectBean {
    private String logTag = ">>>蓝牙BLE-连接对象";

    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mWrite;
    private BluetoothGattCharacteristic mDescriptor;
    private IBleDeviceRuler mIBleDeviceRuler;

    public ConnectBean(IBleDeviceRuler ruler) {
        this.mIBleDeviceRuler = ruler;
    }

    public BluetoothGatt getGatt() {
        return mGatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        mGatt = gatt;
    }

    public boolean hasUUID() {
        return mWrite != null;
    }

    public boolean getCharacteristic(List<BluetoothGattService> gattServices) {
        boolean isSuccess = false;
        if (mIBleDeviceRuler.getGattServiceUUID() == null) {
            for (BluetoothGattService gattService : gattServices) {
                isSuccess = init(gattService);
                if(isSuccess){
                    break;
                }
            }
        } else {
            BluetoothGattService aicareService = mGatt.getService(mIBleDeviceRuler.getGattServiceUUID());
            if (aicareService != null) {
                isSuccess = init(aicareService);
            }
        }
        return isSuccess;
    }

    private boolean init(BluetoothGattService gattService) {
        if (mIBleDeviceRuler.getWriteCharacteristicUUID() != null) {
            mWrite = gattService.getCharacteristic(mIBleDeviceRuler.getWriteCharacteristicUUID());
        }

        if (mIBleDeviceRuler.getDescriptorCharacteristicUUID() != null) {
            mDescriptor = gattService.getCharacteristic(mIBleDeviceRuler.getDescriptorCharacteristicUUID());
        }
        if (mWrite == null || mDescriptor == null) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                if (mWrite == null) {
                    if (mIBleDeviceRuler.isWriteCharacteristic(gattCharacteristic)) {
                        mWrite = gattCharacteristic;
                    }
                } else if (mDescriptor == null) {
                    if (mIBleDeviceRuler.isDescriptorCharacteristic(gattCharacteristic)) {
                        mDescriptor = gattCharacteristic;
                    }
                }

                if (mWrite != null && mDescriptor != null) {
                    break;
                }

            }
        }
        if(mWrite != null && mDescriptor != null){
            enableAicareIndication(mDescriptor);
            return true;
        }
        return false;
    }

    private void enableAicareIndication(BluetoothGattCharacteristic descriptorCharacteristic) {
        mGatt.setCharacteristicNotification(descriptorCharacteristic, true);
        BluetoothGattDescriptor descriptor = descriptorCharacteristic.getDescriptor(mIBleDeviceRuler.getDescriptorUUID());
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mGatt.writeDescriptor(descriptor);

    }


    public void disconnect() {
        if (mGatt != null) {
            mGatt.disconnect();
        }
    }

    public void onDestroy() {
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        mWrite = null;
        mDescriptor = null;
        mIBleDeviceRuler = null;
    }

    public boolean writeCommand(byte[] data) {
        if (mGatt != null && mWrite != null && data != null) {
            mWrite.setValue(data);
            boolean isSuccess = mGatt.writeCharacteristic(mWrite);
            SnbLog.e(logTag, "发送指令:" + (isSuccess ? "成功" : "失败"));
            return isSuccess;
        }
        SnbLog.e(logTag, "发送指令:失败-某个对象不存在");
        return false;
    }
}
