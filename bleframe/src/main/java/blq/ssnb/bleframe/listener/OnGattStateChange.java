package blq.ssnb.bleframe.listener;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-27
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      BLE 连接的状态返回
 * ================================================
 * </pre>
 */
public interface OnGattStateChange extends OnBleError{
    void onConnecting(String address);

    void onConnected(String address);

    void onDisconnecting(String address);

    void onDisconnected(String address);
}
