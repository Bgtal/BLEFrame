package com.blq.ssnb.bleframe.bodyfat;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;

import java.util.UUID;

import blq.ssnb.bleframe.inf.IBleDeviceRuler;

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
public class BleBodyFatRule implements IBleDeviceRuler {

    @Override
    public boolean isDevice(ScanResult result) {
        return result != null
                && result.getDevice() != null
                && result.getDevice().getName() != null
                && result.getDevice().getName().contains("1741");
    }

    @Override
    public UUID getDescriptorUUID() {
        return UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    @Override
    public UUID getGattServiceUUID() {
        return null;
    }

    @Override
    public UUID getWriteCharacteristicUUID() {
        return null;
    }

    @Override
    public UUID getDescriptorCharacteristicUUID() {
        return null;
    }

    @Override
    public boolean isWriteCharacteristic(BluetoothGattCharacteristic gattCharacteristic) {
        return gattCharacteristic.getUuid().toString().toLowerCase().contains("fff6");
    }

    @Override
    public boolean isDescriptorCharacteristic(BluetoothGattCharacteristic gattCharacteristic) {
        return gattCharacteristic.getUuid().toString().toLowerCase().contains("fff7");
    }
}
