package blq.ssnb.bleframe.listener;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 * 蓝牙状态的回调
 * ================================================
 * </pre>
 */
public interface OnBluetoothStateChange extends OnBleError{
    /**
     * 正在打开
     */
    void onTurningOn();

    /**
     * 已经打开
     */
    void onOn();

    /**
     * 正在关闭
     */
    void onTurningOff();

    /**
     * 已经关闭
     */
    void onOff();
}
