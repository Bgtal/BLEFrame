package blq.ssnb.bleframe.listener;

import android.bluetooth.BluetoothDevice;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      蓝牙扫描返回对象
 * ================================================
 * </pre>
 */
public interface OnBleScan extends OnBleError {
    void onScanResult(BluetoothDevice device);
    void onScanTimeOut();
}
