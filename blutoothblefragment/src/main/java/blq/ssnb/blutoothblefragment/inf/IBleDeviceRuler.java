package blq.ssnb.blutoothblefragment.inf;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;

import java.util.UUID;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      蓝牙终端交互用到的相关数据和判断规则
 *      例如 通过返回的扫描对象判断该设备是否是目标设备
 * ================================================
 * </pre>
 */
public interface IBleDeviceRuler {
    /**
     * 扫描的返回设备是否为目标设备
     * @param result 扫描到的设备
     * @return true 表示是我需要的设备
     */
    boolean isDevice(ScanResult result);

    /**
     * BLEl连接时候用到的Gatt描述UUID
     * @return 不为null
     */
    UUID getDescriptorUUID();

    /**
     * 返回GATT 服务的uuid
     * @return 可以为空
     */
    UUID getGattServiceUUID();

    /**
     *  返回写入用的 特征ID
     * @return 当返回null 的时候回调用 {@link #isWriteCharacteristic(BluetoothGattCharacteristic)}
     * 来判断是否是用于写入的特征
     */
    UUID getWriteCharacteristicUUID();

    /**
     *
     *  返回描述用的 特征ID
     * @return 当返回null 的时候回调用 {@link #isDescriptorCharacteristic(BluetoothGattCharacteristic)}
     * 来判断是否是用于描述的特征
     */
    UUID getDescriptorCharacteristicUUID();

    /**
     * 当没有指定 写入UUID 或者 写入UUID 未找到相应的对象的时候会调用该方法
     * @param gattCharacteristic 特征对象
     * @return true 表示该特征对象是写入对象
     */
    boolean isWriteCharacteristic(BluetoothGattCharacteristic gattCharacteristic);

    /**
     * 当没有指定 描述UUID 或者 描述UUID 未找到相应的对象的时候会调用该方法
     * @param gattCharacteristic 特征对象
     * @return true 表示该特征对象是描述对象
     */
    boolean isDescriptorCharacteristic(BluetoothGattCharacteristic gattCharacteristic);

    // TODO: 2019-09-26 增加一个借口用户回调写入的模式

}
