
# BlutoothBLEFrame [![](https://jitpack.io/v/Bgtal/BLEFrame.svg)](https://jitpack.io/#Bgtal/BLEFrame)
> maven
```  
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        ...
    }
}
```

> implementation 'com.github.Bgtal:BLEFrame:1.0.1'

## 项目依赖
* 使用的时候需要在自己的项目中引用下面的包，不然会报错
> implementation 'com.android.support:appcompat-v7:27.1.1'  
> implementation 'com.github.Bgtal:SnbUtil:0.0.8'  
> implementation 'com.android.support:appcompat-v7:27.1.1'

### 说明

该类主要是对蓝牙BLE操作的一些封装
便于匹配不同的设备规则

> demo 有简单的使用
> 代码中也写了比较详细的说明


通过 Google 对 BLE 的介绍我们将 BLE 的使用分为两个部分
* 前端(activiy,fragment)
* 后端(service)

前端用于执行一些对 BLE 的操作指令的发出和回调的展示  
后端用于对蓝牙 BLE 的连接(GATT)管理

#### 后端

对后端分析得出 服务需要两个对象
1. Binder 对象
2. BLE 的通信(GATT)回调对象(BluetoothGattCallback)

然后将服务抽象为 IService 接口  
里面的主要内容是实现服务的 Binder、GATT 的回调和缓存对象(一个map)

##### 服务Binder
经过简单了解和抽象，将蓝牙 BLE 的通讯部分进行了分解
抽象出 三个方法
1. 连接
2. 发送指令
3. 断开连接

```java
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
```
在写Service的 Binder 的时候需要实现该接口
这边提供了 BleBinder 对象进行了实现
如果想要自己实现的话 继承该类即可，如果想要对业务流程进行修改了话
务必参考该类进行修改，否者不能保证能正常运行

##### 通信回调 BluetoothGattCallback
通信回调这里提供了一个 BleGattCallBack 类,该类里面实现了通信的逻辑。如果你想自己写 CallBack 的话，可以选择继承他，或者自己写一个回调


##### 服务类
这里我实现一个服务类 BaseBleService 里面的实现了 IBinder 和 BluetoothGattCallback 的方法
如果需要自己实现逻辑的话，建议按相应实现类的逻辑来实现。

```java
public class BaseBleService extends Service implements IService {
    private IBinder mIBinder;

     @Nullable
     @Override
     public IBinder onBind(Intent intent) {
         if (mIBinder == null) {
             mIBinder = initBinder();
         }
         return mIBinder;
     }

     @Override
     public IBinder initBinder() {
         return new BleBinder(this);
     }

     private BluetoothGattCallback mGattCallback;
     @Override
     public BluetoothGattCallback initBleGattCallBack() {
         return new BleGattCallBack(this);
     }

     @Override
     public BluetoothGattCallback getBleGattCallBack() {
         if(mGattCallback == null){
             mGattCallback = initBleGattCallBack();
         }
         return mGattCallback;
     }

     /**
      *
      * 移除对象只有在两个地方进行
      * 1.连接断开的时候
      * 2.执行连接或断开连接，但是失败的时候
      * 增加对象只有一个地方进行
      * 1.连接设备地址的时候
      */
     private Map<String, ConnectBean> mConnectBeanMap = new HashMap<>();

     @Override
     public Map<String, ConnectBean> initConnectCacheMap() {
         return new HashMap<>();
     }

     @Override
     public Map<String, ConnectBean> getConnectCacheMap() {
         if (mConnectBeanMap == null) {
             mConnectBeanMap = initConnectCacheMap();
         }
         return mConnectBeanMap;
     }

     @Override
     public Context getContext() {
         return this;
     }
   }

```

#### 前端
对于 activity 和 fragment 的使用，这里写了一个 Helper 类来帮助使用使用  
>详情可以查看 BaseBleDeviceHelper 类

Helper 类中 主要是用于连接前端和后端以及管理和分发回调  
Helper 中需要实现蓝牙 BLE 相关的规则,具体查看 IBleDeviceRuler 类中的说明  
需要注意的是，一个 helper 只能连接一个设备，当连接新设备的时候，会自动断开以连接的设备

Helper 的实现比较简单，具体的可以查看 demo 来

### 框架的主要思想
1. 所有的蓝牙连接都通过一个 Service 来管理
2. 针对一个类型的蓝牙设备有对应的配置对象(IBleDeviceRuler)
3. 一个 helper 只管理一种类型的设备
4. 考虑到目前的业务不存在同时连接多个设备的情况，所以一个 helper 只能同时连接一个设备
1. 
5. heper 内部的回调(HelperListenerProxy)只对 Service 通信的进行反应,要想回调到前端，需要用户自己去设置监听(可以看demo)

###
