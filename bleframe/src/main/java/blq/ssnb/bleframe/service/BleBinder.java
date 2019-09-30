package blq.ssnb.bleframe.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Binder;

import blq.ssnb.bleframe.ConnectBean;
import blq.ssnb.bleframe.SnbBluetoothManager;
import blq.ssnb.bleframe.inf.IBleDeviceRuler;
import blq.ssnb.bleframe.inf.IService;
import blq.ssnb.bleframe.inf.IServiceBinder;
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
public class BleBinder extends Binder implements IServiceBinder {

    protected IService mService;

    public BleBinder(IService service) {
        mService = service;
    }

    @Override
    public boolean connectDevice(Context context, String address, IBleDeviceRuler deviceRuler) {
        //从缓存中获取连接对象
        ConnectBean bean = mService.getConnectCacheMap().get(address);
        //如果缓存中没有
        if (bean == null) {
            SnbLog.si(LOG_TAG, getLogStr("调用连接-没有历史"));
            //创建一个
            if (deviceRuler != null) {
                bean = new ConnectBean(deviceRuler);
                mService.getConnectCacheMap().put(address, bean);
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
            if (bean.getGatt() == null) {//null 说明当前对象没有连接过
                BluetoothDevice device = adapter.getRemoteDevice(address);
                BluetoothGatt mGatt = device.connectGatt(context, false, mService.getBleGattCallBack());//连接设备
                bean.setGatt(mGatt);//这里的连接需要注意1.连接的地址可能连接不到，会GATT_STATE 返回会为false false 的时候需要进行移除工作
            } else {//否者说明当前对象连接过
                mService.getBleGattCallBack().onConnectionStateChange(bean.getGatt(), BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);
            }
            SnbLog.si(LOG_TAG, getLogStr("调用连接" + address));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void unConnectDevice(String address) {
        ConnectBean bean = mService.getConnectCacheMap().get(address);
        if (bean != null) {
            bean.disconnect();
        }
    }

    @Override
    public void sendCommand(String address, byte[] data) {
        ConnectBean bean = mService.getConnectCacheMap().get(address);
        if (bean != null) {
            bean.writeCommand(data);
        } else {
            SnbLog.se(LOG_TAG, getLogStr("连接对象为空"));
        }
    }


    private String getLogStr(String msg) {
        return "蓝牙-服务:" + msg;
    }
}
