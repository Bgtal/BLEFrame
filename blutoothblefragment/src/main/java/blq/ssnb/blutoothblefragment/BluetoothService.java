package blq.ssnb.blutoothblefragment;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blq.ssnb.blutoothblefragment.inf.IBleDeviceRuler;
import blq.ssnb.blutoothblefragment.inf.IServiceBinder;
import blq.ssnb.snbutil.SnbLog;

/**
 * <pre>
 * ================================================
 * 作者: BLQ_SSNB
 * 日期：2019-09-26
 * 邮箱: blq_ssnb@outlook.com
 * 修改次数: 1
 * 描述:
 *      蓝牙BLE服务对象
 * ================================================
 * </pre>
 */
public class BluetoothService extends Service {

    /**
     * 发送广播消息
     *
     * @param action
     * @param bundle
     */
    private void sendBleBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent(action);
        if (bundle == null) {
            bundle = new Bundle();
        }
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    private void sendErrorBroadcast(String address,int errorCode, String errorMsg) {
        Bundle bundle = new Bundle();
        bundle.putString(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS, address);
        bundle.putInt(Constant.BroadcastActionAndKey.ERROR_INT_ERROR_CODE, errorCode);
        bundle.putString(Constant.BroadcastActionAndKey.ERROR_STR_ERROR_MSG, errorMsg);
        sendBleBroadcast(Constant.BroadcastActionAndKey.ACTION_ERROR, bundle);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
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
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ConnectBean bean = mConnectBeanMap.get(gatt.getDevice().getAddress());
                if (bean == null) {
                    // TODO: 2019-09-26 通知绑定失败-这理论是不应该出现，说明代码在到这不的时候断开了
                    SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt连接状态变化:未找到对应的Bean"));
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS, gatt.getDevice().getAddress());
                bundle.putInt(Constant.BroadcastActionAndKey.GATT_CONNECTION_STATE_CHANGE_KEY_INT_STATUS, newState);
                String str = "未知状态";
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                    str = "已经连接";
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bean.onDestroy();
                    mConnectBeanMap.remove(gatt.getDevice().getAddress());
                    str = "已断开";
                }

                str = newState == BluetoothProfile.STATE_CONNECTING ? "正在连接"
                        : newState == BluetoothProfile.STATE_DISCONNECTING ? "正在断开" : str;
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt连接状态变化:" + str));
                sendBleBroadcast(Constant.BroadcastActionAndKey.ACTION_GATT_CONNECTION_STATE_CHANGE, bundle);
            } else {
                // TODO: 2019-09-26 这里需要通知状态变化失败
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt连接状态变化:返回状态不为Success"));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ConnectBean bean = mConnectBeanMap.get(gatt.getDevice().getAddress());
                if (bean == null) {
                    // TODO: 2019-09-26 通知绑定失败-这理论是不应该出现，说明代码在到这不的时候断开了

                    SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt服务被发现:未找到对应的Bean"));
                    return;
                }
                List<BluetoothGattService> gattServices = gatt.getServices();
                if (gattServices == null) {
                    // TODO: 2019-09-26 这里发送失败通知
                    SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt服务被发现:GATT服务列表为空"));
                    return;
                }
                boolean isSuccess = bean.getCharacteristic(gattServices);
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt服务被发现:初始化写入相关:" + (isSuccess ? "成功" : "失败")));

            } else {
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt服务被发现:返回状态不为Success"));
            }
        }

        /**
         * 当 GATT 调用  {@link BluetoothGatt#readCharacteristic} 方法的时候回调
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ConnectBean bean = mConnectBeanMap.get(gatt.getDevice().getAddress());
                if (bean == null) {
                    // TODO: 2019-09-26 通知绑定失败-这理论是不应该出现，说明代码在到这不的时候断开了
                    SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Read 方法返回:未找到对应的Bean"));
                    return;
                }

                String address = gatt.getDevice().getAddress();
                byte[] data = characteristic.getValue();

                Bundle bundle = new Bundle();
                bundle.putString(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS, address);
                bundle.putByteArray(Constant.BroadcastActionAndKey.GATT_COMMAND_RESULT_BYTE_ARRAY_DATA, data);

                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Read 方法返回:发送广播"));
                sendBleBroadcast(Constant.BroadcastActionAndKey.ACTION_GATT_COMMAND_RESULT, bundle);

            } else {
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Read 方法返回:返回状态不为Success"));
            }
        }

        /**
         * 当 GATT 调用 {@link BluetoothGatt#writeCharacteristic} 方法的时候回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ConnectBean bean = mConnectBeanMap.get(gatt.getDevice().getAddress());
                if (bean == null) {
                    // TODO: 2019-09-26 通知绑定失败-这理论是不应该出现，说明代码在到这不的时候断开了
                    SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Write 方法返回:未找到对应的Bean"));
                    return;
                }

                String address = gatt.getDevice().getAddress();
                byte[] data = characteristic.getValue();

                Bundle bundle = new Bundle();
                bundle.putString(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS, address);
                bundle.putByteArray(Constant.BroadcastActionAndKey.GATT_COMMAND_RESULT_BYTE_ARRAY_DATA, data);

                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Write 方法返回:发送广播"));
                sendBleBroadcast(Constant.BroadcastActionAndKey.ACTION_GATT_COMMAND_RESULT, bundle);

            } else {
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Write 方法返回:返回状态不为Success"));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            ConnectBean bean = mConnectBeanMap.get(gatt.getDevice().getAddress());
            if (bean == null) {
                // TODO: 2019-09-26 通知绑定失败-这理论是不应该出现，说明代码在到这不的时候断开了
                SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Changed 方法返回:未找到对应的Bean"));
                return;
            }

            String address = gatt.getDevice().getAddress();
            byte[] data = characteristic.getValue();

            Bundle bundle = new Bundle();
            bundle.putString(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS, address);
            bundle.putByteArray(Constant.BroadcastActionAndKey.GATT_COMMAND_RESULT_BYTE_ARRAY_DATA, data);
            SnbLog.se(Constant.LOG_TAG, getLogStr("Gatt Changed 方法返回:发送广播"));
            sendBleBroadcast(Constant.BroadcastActionAndKey.ACTION_GATT_COMMAND_RESULT, bundle);

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        /**
         * 当调用 {@link BluetoothGatt#readRemoteRssi} 方法的时候的回调
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String address = gatt.getDevice().getAddress();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.BroadcastActionAndKey.GLOBAL_KEY_STR_ADDRESS, address);
                bundle.putInt(Constant.BroadcastActionAndKey.GATT_RSSI_RESULT_INT_RSSI, rssi);
                sendBleBroadcast(Constant.BroadcastActionAndKey.ACTION_GATT_RSSI_RESULT, bundle);

            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        SnbLog.se(Constant.LOG_TAG, "服务绑定");
        return new BleBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SnbLog.se(Constant.LOG_TAG, getLogStr("服务解绑"));
        return super.onUnbind(intent);
    }

    public class BleBinder extends Binder implements IServiceBinder {

        public BleBinder() {
        }

        @Override
        public void connectDevice(Context context, String address, IBleDeviceRuler deviceRuler) {
            ConnectBean bean = mConnectBeanMap.get(address);
            if (bean == null) {
                SnbLog.si(Constant.LOG_TAG, getLogStr("调用连接-没有历史"));
                if (deviceRuler != null) {
                    bean = new ConnectBean(deviceRuler);
                    mConnectBeanMap.put(address, bean);
                } else {
                    throw new IllegalArgumentException("请先实例化IBleDeviceRuler对象");
                }
            }
//            if (bean.getGatt() != null) {
//                if (bean.getGatt().getDevice().getAddress().equals(address)) {
//                    SnbLog.w(logTag,"调用连接-两次连接地址一样");
//                    //如果两次连接设备一样，说明xxx
//                    return;
//                } else {
//                    //先断开连接
//                    SnbLog.w(logTag,"调用连接-没有历史");
//                    bean.getGatt().disconnect();
//                    bean.setGatt(null);
//                }
//            }

            BluetoothAdapter adapter = SnbBluetoothManager.singleton().getBluetoothAdapter();
            if (adapter != null) {

                BluetoothDevice device = adapter.getRemoteDevice(address);
                BluetoothGatt mGatt = device.connectGatt(context, false, mGattCallback);//连接设备
                bean.setGatt(mGatt);
                SnbLog.si(Constant.LOG_TAG, getLogStr("调用连接" + bean.getGatt().getDevice().getAddress()));
            }
        }

        @Override
        public void unConnectDevice(String address) {
            ConnectBean bean = mConnectBeanMap.get(address);
            if (bean != null) {
                bean.disconnect();
            }
        }

        @Override
        public void sendCommand(String address, byte[] data) {
            ConnectBean bean = mConnectBeanMap.get(address);
            if (bean != null) {
                bean.writeCommand(data);
            } else {
                SnbLog.se(Constant.LOG_TAG, getLogStr("连接对象为空"));
            }
        }
    }


    private Map<String, ConnectBean> mConnectBeanMap = new HashMap<>();


    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }

}
