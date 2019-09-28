package blq.ssnb.bleframe;

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
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blq.ssnb.bleframe.inf.IBleDeviceRuler;
import blq.ssnb.bleframe.inf.IServiceBinder;
import blq.ssnb.snbutil.SnbLog;

import static blq.ssnb.bleframe.Constant.LOG_TAG;

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

    public static final String BUNDKE_KEY_DEVICE_ADDRESS = "deviceAddress";

    private BluetoothGattCallback mGattCallback;

    protected BluetoothGattCallback getBleGattCallback() {
        if (mGattCallback == null) {
            mGattCallback = initBluetoothGattCallback();
            if (mGattCallback == null) {
                mGattCallback = normalBluetoothGattCallback();
            }
        }
        return mGattCallback;
    }

    /**
     * 这个方式是为了预留给子类
     * 如果有特殊需求需要监听修改callBack的话可以重写该方法
     */
    protected BluetoothGattCallback initBluetoothGattCallback() {
        return normalBluetoothGattCallback();
    }

    private BluetoothGattCallback normalBluetoothGattCallback() {
        return new BluetoothGattCallback() {
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
                ConnectBean bean = getConnectBeanMap().get(address);

                if (status == BluetoothGatt.GATT_SUCCESS) {

                    String str = "未知状态";
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt.discoverServices();
                        str = "已经连接";
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (bean != null) {
                            bean.onDestroy();
                            getConnectBeanMap().remove(address);
                        } else {
                            gatt.close();
                        }
                        str = "已断开";
                    }

                    str = newState == BluetoothProfile.STATE_CONNECTING ? "正在连接"
                            : newState == BluetoothProfile.STATE_DISCONNECTING ? "正在断开" : str;

                    SnbLog.se(LOG_TAG, getLogStr("Gatt连接状态变化:" + str));

                    SendBroadcastUtil.sendGattStateChange(getBaseContext(), true, address, newState);

                } else {
                    SnbLog.se(LOG_TAG, getLogStr("Gatt连接状态变化:返回状态不为Success: 状态为：" + address + "===" + status));
                    SendBroadcastUtil.sendGattStateChange(getBaseContext(), false, address, -1);
                    if(bean!=null){
                        //如果连接失败调用
                        //那就断开连接
                        bean.disconnect();
                        //并且清空掉他
                        bean.onDestroy();
                        //从列表中移除
                        getConnectBeanMap().remove(address);
                    }

                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                String address = gatt.getDevice().getAddress();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    ConnectBean bean = getConnectBeanMap().get(gatt.getDevice().getAddress());
                    if (bean == null) {
                        SnbLog.se(LOG_TAG, getLogStr("Gatt服务被发现:未找到对应的Bean"));
                        //未找到对应的对象说明无法进行匹配工作，让这个链接直接断掉吧
                        //因为在这个监听里面的对象都应该是加入到缓存中的
                        gatt.disconnect();
                        gatt.close();
                        SendBroadcastUtil.sendServicesDiscovered(getBaseContext(), address, false);
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
                    SendBroadcastUtil.sendServicesDiscovered(getBaseContext(), address, isSuccess);

                } else {
                    SnbLog.se(LOG_TAG, getLogStr("Gatt服务被发现:返回状态不为Success"));
                    SendBroadcastUtil.sendServicesDiscovered(getBaseContext(), address, false);
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
                SendBroadcastUtil.sendGattCommandResult(getBaseContext(), true, address, data);
            }

            private void gattCommunicationDispose(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                String address = gatt.getDevice().getAddress();

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    byte[] data = characteristic.getValue();
                    SnbLog.se(LOG_TAG, getLogStr("Gatt 读写方法返回:发送广播"));
                    SendBroadcastUtil.sendGattCommandResult(getBaseContext(), true, address, data);
                } else {
                    SnbLog.se(LOG_TAG, getLogStr("Gatt 读写方法返回状态不为Success:" + status));
                    SendBroadcastUtil.sendGattCommandResult(getBaseContext(), false, address, new byte[]{});
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
                    SendBroadcastUtil.sendRemoteRssi(getBaseContext(), true, address, rssi);
                } else {
                    SnbLog.se(LOG_TAG, getLogStr("读取设备信号强度状态不为Success:" + status));
                    SendBroadcastUtil.sendRemoteRssi(getBaseContext(), false, address, rssi);
                }
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
                String address = gatt.getDevice().getAddress();
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    SnbLog.se(LOG_TAG, getLogStr("设置传输包大小:" + mtu));
                    SendBroadcastUtil.sendMTUChange(getBaseContext(), true, address, mtu);
                } else {
                    SnbLog.se(LOG_TAG, getLogStr("设置传输包的大小失败:" + status));
                    SendBroadcastUtil.sendMTUChange(getBaseContext(), false, address, mtu);
                }
            }

        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        SnbLog.se(LOG_TAG, "服务绑定");
//        Bundle bundle = new Bundle();
//        bundle.putInt("ss",0);
//        sendBleBroadcast(BroadcastActionAndKey.ACTION_SERVICE_STATUS,bundle);
        return new BleBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SnbLog.se(LOG_TAG, getLogStr("服务解绑"));
//        Bundle bundle = new Bundle();
//        bundle.putInt("ss",1);
//        sendBleBroadcast(BroadcastActionAndKey.ACTION_SERVICE_STATUS,bundle);
        return super.onUnbind(intent);
    }

    public class BleBinder extends Binder implements IServiceBinder {

        public BleBinder() {
        }

        @Override
        public boolean connectDevice(Context context, String address, IBleDeviceRuler deviceRuler) {
            //从缓存中获取连接对象
            ConnectBean bean = getConnectBeanMap().get(address);
            //如果缓存中没有
            if (bean == null) {
                SnbLog.si(LOG_TAG, getLogStr("调用连接-没有历史"));
                //创建一个
                if (deviceRuler != null) {
                    bean = new ConnectBean(deviceRuler);
                    getConnectBeanMap().put(address, bean);
                } else {
                    throw new IllegalArgumentException("请先实例化IBleDeviceRuler对象");
                }
            }
            //连接对象中的协议如果不为null 说明已经有连接过了
//            if (bean.getGatt() != null) {
                //验证下连接的地址是不是一样的（基本不会出现不一样的情况）
//                if (bean.getGatt().getDevice().getAddress().equals(address)) {
//                SnbLog.si(LOG_TAG, getLogStr("调用连接-已存在连接对象"));
                //再次调用连接方法
//                boolean b = bean.getGatt().connect();
//                SnbLog.si(LOG_TAG, getLogStr("调用连接-已存在连接对象:" + b));
//                return true;
//                } else {
                //先断开连接
//                    SnbLog.si(LOG_TAG, getLogStr("调用连接-先断开不对的连接:" + address));
//                    bean.disconnect();
                //清空所有的东西
//                    bean.onDestroy();//这里会收到GATT的连接状态的改变
//                }

                /**
                 * 上面的注释掉的地方，
                 * 因为没有地方能够存入一个key 和连接地址不一样的地方，
                 * 所以也取出来的GATT不可能地址不一样，所以没必要进行判断
                 *
                 * 其实连接协议存在的情况下，不用进行connect方法，因为一旦disconnect
                 * 缓存中就会移除该对象。所有不会有上面的情况
                 */
//            }


            //当不存在连接协议的时候
            BluetoothAdapter adapter = SnbBluetoothManager.singleton().getBluetoothAdapter();
            if (adapter != null) {
                if(bean.getGatt() ==null){//null 说明当前对象没有连接过
                    BluetoothDevice device = adapter.getRemoteDevice(address);
                    BluetoothGatt mGatt = device.connectGatt(context, false, getBleGattCallback());//连接设备
                    bean.setGatt(mGatt);//这里的连接需要注意1.连接的地址可能连接不到，会GATT_STATE 返回会为false false 的时候需要进行移除工作
                }else{//否者说明当前对象连接过
                    mGattCallback.onConnectionStateChange(bean.getGatt(),BluetoothGatt.GATT_SUCCESS,BluetoothProfile.STATE_CONNECTED);
                }
                SnbLog.si(LOG_TAG, getLogStr("调用连接" + address));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void unConnectDevice(String address) {
            ConnectBean bean = getConnectBeanMap().get(address);
            if (bean != null) {
                bean.disconnect();
            }
        }

        @Override
        public void sendCommand(String address, byte[] data) {
            ConnectBean bean = getConnectBeanMap().get(address);
            if (bean != null) {
                bean.writeCommand(data);
            } else {
                SnbLog.se(LOG_TAG, getLogStr("连接对象为空"));
            }
        }
    }

    private Map<String, ConnectBean> mConnectBeanMap = new HashMap<>();

    //移除对象只有在两个地方进行
    //1.连接断开的时候
    //2.执行连接或断开连接，但是失败的时候
    //增加对象只有一个地方进行
    //1.连接设备地址的时候
    protected Map<String, ConnectBean> getConnectBeanMap() {
        return mConnectBeanMap;
    }


    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }

}
