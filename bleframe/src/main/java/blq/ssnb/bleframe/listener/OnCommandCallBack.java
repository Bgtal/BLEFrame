package blq.ssnb.bleframe.listener;

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
public interface OnCommandCallBack extends OnBleError {

    void onGattRSSIResult(int rssi);
    void onSetMTUResult(int newSize);
    void onCommandResult(byte[] data);
}
