package blq.ssnb.bleframe.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.List;

import blq.ssnb.bleframe.ConnectBean;
import blq.ssnb.bleframe.SendBroadcastUtil;
import blq.ssnb.bleframe.inf.IService;
import blq.ssnb.snbutil.SnbLog;

import static blq.ssnb.bleframe.Constant.LOG_TAG;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-29
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      添加描述
 * ================================================
 * </pre>
 */
public class BleGattCallBack extends BluetoothGattCallback {

    protected IService mIService;

    BleGattCallBack(IService service){
        this.mIService = service;
    }

    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyRead(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        String address = gatt.getDevice().getAddress();
        ConnectBean bean = mIService.getConnectCacheMap().get(address);

        if (status == BluetoothGatt.GATT_SUCCESS) {

            String str = "未知状态";
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                str = "已经连接";
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (bean != null) {
                    bean.onDestroy();
                    mIService.getConnectCacheMap().remove(address);
                } else {
                    gatt.close();
                }
                str = "已断开";
            }

            str = newState == BluetoothProfile.STATE_CONNECTING ? "正在连接"
                    : newState == BluetoothProfile.STATE_DISCONNECTING ? "正在断开" : str;

            SnbLog.se(LOG_TAG, getLogStr("Gatt连接状态变化:" + str));

            SendBroadcastUtil.sendGattStateChange(mIService.getContext(), true, address, newState);

        } else {
            SnbLog.se(LOG_TAG, getLogStr("Gatt连接状态变化:返回状态不为Success: 状态为：" + address + "===" + status));
            SendBroadcastUtil.sendGattStateChange(mIService.getContext(), false, address, -1);
            if (bean != null) {
                //如果连接失败调用
                //那就断开连接
                bean.disconnect();
                //并且清空掉他
                bean.onDestroy();
                //从列表中移除
                mIService.getConnectCacheMap().remove(address);
            }

        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        String address = gatt.getDevice().getAddress();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            ConnectBean bean = mIService.getConnectCacheMap().get(gatt.getDevice().getAddress());
            if (bean == null) {
                SnbLog.se(LOG_TAG, getLogStr("Gatt服务被发现:未找到对应的Bean"));
                //未找到对应的对象说明无法进行匹配工作，让这个链接直接断掉吧
                //因为在这个监听里面的对象都应该是加入到缓存中的
                gatt.disconnect();
                gatt.close();
                SendBroadcastUtil.sendServicesDiscovered(mIService.getContext(), address, false);
                return;
            }

            boolean isSuccess;

            List<BluetoothGattService> gattServices = gatt.getServices();
            if (gattServices == null) {
                isSuccess = false;
                SnbLog.se(LOG_TAG, getLogStr("Gatt服务被发现:GATT服务列表为空"));
            } else {
                isSuccess = bean.getCharacteristic(gattServices);
                SnbLog.se(LOG_TAG, getLogStr("Gatt服务被发现:初始化写入相关:" + (isSuccess ? "成功" : "失败")));
            }
            SendBroadcastUtil.sendServicesDiscovered(mIService.getContext(), address, isSuccess);

        } else {
            SnbLog.se(LOG_TAG, getLogStr("Gatt服务被发现:返回状态不为Success"));
            SendBroadcastUtil.sendServicesDiscovered(mIService.getContext(), address, false);
        }
    }

    /**
     * 当 GATT 调用  {@link BluetoothGatt#readCharacteristic} 方法的时候回调
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        SnbLog.se(LOG_TAG, getLogStr("Gatt Read 方法通信"));
//                gattCommunicationDispose(gatt, characteristic, status);
        // TODO: 2019-09-28 这里和下面需要写两个返回方法
    }

    /**
     * 当 GATT 调用 {@link BluetoothGatt#writeCharacteristic} 方法的时候回调
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        SnbLog.se(LOG_TAG, getLogStr("Gatt Write 方法通信"));
//                gattCommunicationDispose(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String address = gatt.getDevice().getAddress();
        byte[] data = characteristic.getValue();
        SnbLog.se(LOG_TAG, getLogStr("Gatt 读写改变-发送广播"));
        SendBroadcastUtil.sendGattCommandResult(mIService.getContext(), true, address, data);
    }

    private void gattCommunicationDispose(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        String address = gatt.getDevice().getAddress();

        if (status == BluetoothGatt.GATT_SUCCESS) {
            byte[] data = characteristic.getValue();
            SnbLog.se(LOG_TAG, getLogStr("Gatt 读写方法返回:发送广播"));
            SendBroadcastUtil.sendGattCommandResult(mIService.getContext(), true, address, data);
        } else {
            SnbLog.se(LOG_TAG, getLogStr("Gatt 读写方法返回状态不为Success:" + status));
            SendBroadcastUtil.sendGattCommandResult(mIService.getContext(), false, address, new byte[]{});
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        SnbLog.se(LOG_TAG, getLogStr("Gatt onDescriptorRead 方法通信"));
        gattDescriptorDispose(gatt, descriptor, status);

    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        SnbLog.se(LOG_TAG, getLogStr("Gatt onDescriptorWrite 方法通信"));
        gattDescriptorDispose(gatt, descriptor, status);

    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }

    public void gattDescriptorDispose(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        // TODO: 2019-09-27 这个不知道干嘛用到，展示不写广播
        String address = gatt.getDevice().getAddress();
        if (status == BluetoothGatt.GATT_SUCCESS) {
//                    descriptor.getUuid();
//                    descriptor.getValue();
            SnbLog.se(LOG_TAG, getLogStr("gattDescriptorDispose:展示不知道干嘛用的"));

        } else {

            SnbLog.se(LOG_TAG, getLogStr("Gatt Descriptor 读写方法返回状态不为Success:" + status));
        }
    }


    /**
     * 当调用 {@link BluetoothGatt#readRemoteRssi} 方法的时候的回调
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        String address = gatt.getDevice().getAddress();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            SnbLog.se(LOG_TAG, getLogStr("读取设备信号强度状态返回:" + rssi));
            SendBroadcastUtil.sendRemoteRssi(mIService.getContext(), true, address, rssi);
        } else {
            SnbLog.se(LOG_TAG, getLogStr("读取设备信号强度状态不为Success:" + status));
            SendBroadcastUtil.sendRemoteRssi(mIService.getContext(), false, address, rssi);
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        String address = gatt.getDevice().getAddress();
        if (status == BluetoothGatt.GATT_SUCCESS) {

            SnbLog.se(LOG_TAG, getLogStr("设置传输包大小:" + mtu));
            SendBroadcastUtil.sendMTUChange(mIService.getContext(), true, address, mtu);
        } else {
            SnbLog.se(LOG_TAG, getLogStr("设置传输包的大小失败:" + status));
            SendBroadcastUtil.sendMTUChange(mIService.getContext(), false, address, mtu);
        }
    }

    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }
}
