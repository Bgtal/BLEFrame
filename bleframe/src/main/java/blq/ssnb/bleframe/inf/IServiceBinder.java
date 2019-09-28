package blq.ssnb.bleframe.inf;

import android.content.Context;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      service binder 对象
 *      主要分为ble 的三个接口
 *      1.连接
 *      2.断开
 *      3.下发指令
 * ================================================
 * </pre>
 */
public interface IServiceBinder {
    /**
     * 连接设备
     * @param context 上下文对象
     * @param address 设备地址
     * @param deviceRuler 设备的相关规则
     */
    boolean connectDevice(Context context, String address, IBleDeviceRuler deviceRuler);

    /**
     * 断开连接设备
     * @param address 要断开的设备地址
     */
    void unConnectDevice(String address);

    /**
     * 发送指令
     * @param address 设备地址
     * @param data 指令数据
     */
    void sendCommand(String address, byte[] data);
}
